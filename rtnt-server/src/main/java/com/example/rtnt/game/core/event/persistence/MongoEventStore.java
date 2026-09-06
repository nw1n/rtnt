package com.example.rtnt.game.core.event.persistence;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.weather.TemperatureChanged;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@NullMarked
public class MongoEventStore implements EventStore {
    private final EventMongoRepository eventMongoRepository;

    public MongoEventStore(EventMongoRepository eventMongoRepository) {
        this.eventMongoRepository = eventMongoRepository;
    }

    @Override
    public void append(TemperatureChanged event) {
        long version = this.eventMongoRepository.findTopByStreamIdOrderByStreamVersionDesc(WORLD_STREAM)
                .map(document -> document.streamVersion() + 1)
                .orElse(1L);
        this.eventMongoRepository.save(new EventDocument(
                UUID.randomUUID().toString(),
                WORLD_STREAM,
                version,
                event.tick(),
                TemperatureChanged.TYPE,
                Map.of("delta", event.delta()),
                Instant.now()
        ));
    }

    @Override
    public List<TemperatureChanged> readAll() {
        return this.eventMongoRepository.findByStreamIdOrderByStreamVersionAsc(WORLD_STREAM).stream()
                .map(MongoEventStore::toEvent)
                .toList();
    }

    private static TemperatureChanged toEvent(EventDocument document) {
        if (!TemperatureChanged.TYPE.equals(document.type())) {
            throw new IllegalArgumentException("unknown event type: " + document.type());
        }
        Object delta = document.payload().get("delta");
        if (!(delta instanceof Number number)) {
            throw new IllegalArgumentException("missing numeric payload field: delta");
        }
        return new TemperatureChanged(document.tick(), number.intValue());
    }
}
