package com.example.rtnt.adapter.out.publish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.example.rtnt.usecase.ship.TradeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    private static final List<String> DOTENV_FILES = List.of(".env.secrets.local", ".env.local");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final String bootstrapServers;
    private final String securityProtocol;
    private final String saslMechanism;
    private final String saslUsername;
    private final String saslPassword;
    private final String sslEndpointIdentificationAlgorithm;
    private final String sslTruststoreType;
    private final String sslTruststoreLocation;
    private final String sslTruststorePassword;
    private final Properties dotenvCache = new Properties();
    private boolean dotenvLoaded = false;

    public Producer(
            @Value("${rtnt.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${rtnt.kafka.security-protocol:SASL_SSL}") String securityProtocol,
            @Value("${rtnt.kafka.sasl-mechanism:SCRAM-SHA-256}") String saslMechanism,
            @Value("${rtnt.kafka.sasl-username}") String saslUsername,
            @Value("${rtnt.kafka.sasl-password}") String saslPassword,
            @Value("${rtnt.kafka.ssl-endpoint-identification-algorithm:}") String sslEndpointIdentificationAlgorithm,
            @Value("${rtnt.kafka.ssl-truststore-type:PKCS12}") String sslTruststoreType,
            @Value("${rtnt.kafka.ssl-truststore-location}") String sslTruststoreLocation,
            @Value("${rtnt.kafka.ssl-truststore-password}") String sslTruststorePassword
    ) {
        this.bootstrapServers = bootstrapServers;
        this.securityProtocol = securityProtocol;
        this.saslMechanism = saslMechanism;
        this.saslUsername = saslUsername;
        this.saslPassword = saslPassword;
        this.sslEndpointIdentificationAlgorithm = sslEndpointIdentificationAlgorithm;
        this.sslTruststoreType = sslTruststoreType;
        this.sslTruststoreLocation = sslTruststoreLocation;
        this.sslTruststorePassword = sslTruststorePassword;
    }

    private Properties buildProps() {
        String resolvedBootstrapServers = this.requiredConfig("RTNT_KAFKA_BOOTSTRAP_SERVERS", this.bootstrapServers);
        String resolvedSaslUsername = this.requiredConfig("RTNT_KAFKA_SASL_USERNAME", this.saslUsername);
        String resolvedSaslPassword = this.requiredConfig("RTNT_KAFKA_SASL_PASSWORD", this.saslPassword);
        String resolvedTruststoreLocation = this.requiredConfig("RTNT_KAFKA_SSL_TRUSTSTORE_LOCATION", this.sslTruststoreLocation);
        String resolvedTruststorePassword = this.requiredConfig("RTNT_KAFKA_SSL_TRUSTSTORE_PASSWORD", this.sslTruststorePassword);

        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasConfig = String.format(jaasTemplate, resolvedSaslUsername, resolvedSaslPassword);

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", resolvedBootstrapServers);
        props.setProperty("security.protocol", this.securityProtocol);
        props.setProperty("sasl.mechanism", this.saslMechanism);
        props.setProperty("sasl.jaas.config", jaasConfig);
        props.setProperty("ssl.endpoint.identification.algorithm", this.sslEndpointIdentificationAlgorithm);
        props.setProperty("ssl.truststore.type", this.sslTruststoreType);
        props.setProperty("ssl.truststore.location", resolvedTruststoreLocation);
        props.setProperty("ssl.truststore.password", resolvedTruststorePassword);
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());
        props.setProperty("request.timeout.ms", "10000");
        props.setProperty("delivery.timeout.ms", "12000");
        props.setProperty("max.block.ms", "10000");
        return props;
    }

    public void produceTradeEvent(TradeEvent tradeEvent) {
        Properties props = this.buildProps();
        try {
            String payload = OBJECT_MAPPER.writeValueAsString(tradeEvent);
            try {
                this.produceRecord(props, KafkaTopic.TRADE_EVENT.topicName(), tradeEvent.shipId(), payload);
            } catch (Exception primarySendException) {
                LOGGER.warn(
                        "Failed to send trade event to topic {}. Falling back to topic {}. Cause: {}",
                        KafkaTopic.TRADE_EVENT.topicName(),
                        KafkaTopic.WORLD_STATE.topicName(),
                        primarySendException.getMessage()
                );
                this.produceRecord(props, KafkaTopic.WORLD_STATE.topicName(), tradeEvent.shipId(), payload);
            }
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize trade event payload", exception);
        }
    }

    private void produceRecord(Properties props, String topicName, String key, String message) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        try {
            Future<RecordMetadata> result = producer.send(new ProducerRecord<>(topicName, key, message));
            RecordMetadata recordData = result.get();
            LOGGER.info(
                    "Kafka message sent: topic={}, partition={}, offset={}, key={}",
                    topicName,
                    recordData.partition(),
                    recordData.offset(),
                    key
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka send interrupted", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Kafka send failed", ex);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    private String requiredConfig(String envKey, String currentValue) {
        if (currentValue != null && !currentValue.isBlank()) {
            return currentValue;
        }
        this.ensureDotenvLoaded();
        String dotenvValue = this.dotenvCache.getProperty(envKey);
        if (dotenvValue == null || dotenvValue.isBlank()) {
            throw new IllegalStateException("Missing required Kafka config (and env fallback): " + envKey);
        }
        return dotenvValue;
    }

    private synchronized void ensureDotenvLoaded() {
        if (this.dotenvLoaded) {
            return;
        }
        for (String dotenvFile : DOTENV_FILES) {
            Path path = Path.of(dotenvFile);
            if (!Files.exists(path)) {
                continue;
            }
            try {
                for (String rawLine : Files.readAllLines(path)) {
                    String line = rawLine.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("export ")) {
                        line = line.substring("export ".length()).trim();
                    }
                    int separatorIndex = line.indexOf('=');
                    if (separatorIndex <= 0) {
                        continue;
                    }
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();
                    if ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    this.dotenvCache.setProperty(key, value);
                }
            } catch (IOException ignored) {
                // Missing fallback keys are validated in requiredConfig.
            }
        }
        this.dotenvLoaded = true;
    }
}
