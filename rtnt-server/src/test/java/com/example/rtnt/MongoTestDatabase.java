package com.example.rtnt;

import org.springframework.data.mongodb.core.MongoTemplate;

final class MongoTestDatabase {
    static final String NAME = "rtnt-test";

    private MongoTestDatabase() {
    }

    static void drop(MongoTemplate mongoTemplate) {
        String name = mongoTemplate.getDb().getName();
        if (!NAME.equals(name)) {
            throw new IllegalStateException(
                    "Refusing to drop Mongo database '" + name + "'; tests must use '" + NAME + "'"
            );
        }
        mongoTemplate.getDb().drop();
    }
}
