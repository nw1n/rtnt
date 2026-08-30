package com.example.rtnt;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class WipeMongoDatabaseOnStartup implements ApplicationRunner {
    private final MongoTemplate mongoTemplate;

    WipeMongoDatabaseOnStartup(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        MongoTestDatabase.drop(this.mongoTemplate);
    }
}
