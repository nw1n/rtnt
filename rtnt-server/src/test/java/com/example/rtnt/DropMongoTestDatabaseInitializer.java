package com.example.rtnt;

import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class DropMongoTestDatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        String uri = applicationContext.getEnvironment().getProperty("spring.mongodb.uri");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("spring.mongodb.uri must be set for tests");
        }
        MongoTestDatabase.drop(uri);
    }
}
