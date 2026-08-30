package com.example.rtnt;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;

final class MongoTestDatabase {
    static final String NAME = "rtnt-test";

    private MongoTestDatabase() {
    }

    static void drop(String mongoUri) {
        var connectionString = new ConnectionString(mongoUri);
        String database = connectionString.getDatabase();
        if (!NAME.equals(database)) {
            throw new IllegalStateException(
                    "Refusing to drop Mongo database '" + database + "'; tests must use '" + NAME + "'"
            );
        }
        try (var client = MongoClients.create(connectionString)) {
            client.getDatabase(database).drop();
        }
    }
}
