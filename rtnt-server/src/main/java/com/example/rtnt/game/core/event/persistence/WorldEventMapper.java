package com.example.rtnt.game.core.event.persistence;

import com.example.rtnt.game.core.event.IslandCreated;
import com.example.rtnt.game.core.event.IslandPopulationGrew;
import com.example.rtnt.game.core.event.WorldCleared;
import com.example.rtnt.game.core.event.WorldEvent;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public final class WorldEventMapper {
    private WorldEventMapper() {
    }

    public static EventDocument toDocument(String streamId, long streamVersion, WorldEvent event, Instant recordedAt) {
        return new EventDocument(
                UUID.randomUUID().toString(),
                streamId,
                streamVersion,
                event.tick(),
                event.type(),
                payload(event),
                recordedAt
        );
    }

    public static WorldEvent toEvent(EventDocument document) {
        Map<String, Object> payload = document.payload();
        return switch (document.type()) {
            case IslandCreated.TYPE -> new IslandCreated(
                    document.tick(),
                    stringValue(payload, "islandId"),
                    stringValue(payload, "name"),
                    intValue(payload, "x"),
                    intValue(payload, "y"),
                    intValue(payload, "width"),
                    intValue(payload, "length")
            );
            case IslandPopulationGrew.TYPE -> new IslandPopulationGrew(
                    document.tick(),
                    stringValue(payload, "islandId"),
                    longValue(payload, "amount")
            );
            case WorldCleared.TYPE -> new WorldCleared(document.tick());
            default -> throw new IllegalArgumentException("unknown event type: " + document.type());
        };
    }

    private static Map<String, Object> payload(WorldEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        switch (event) {
            case IslandCreated created -> {
                payload.put("islandId", created.islandId());
                payload.put("name", created.name());
                payload.put("x", created.x());
                payload.put("y", created.y());
                payload.put("width", created.width());
                payload.put("length", created.length());
            }
            case IslandPopulationGrew grew -> {
                payload.put("islandId", grew.islandId());
                payload.put("amount", grew.amount());
            }
            case WorldCleared ignored -> {
            }
        }
        return payload;
    }

    private static String stringValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            throw new IllegalArgumentException("missing payload field: " + key);
        }
        return value.toString();
    }

    private static int intValue(Map<String, Object> payload, String key) {
        return Math.toIntExact(longValue(payload, key));
    }

    private static long longValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("missing numeric payload field: " + key);
    }
}
