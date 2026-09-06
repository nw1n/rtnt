package com.example.rtnt.game.core.event.persistence;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "events")
@CompoundIndex(name = "stream_version", def = "{'streamId': 1, 'streamVersion': 1}", unique = true)
@NullMarked
public record EventDocument(
        @Id String id,
        String streamId,
        long streamVersion,
        long tick,
        String type,
        Map<String, Object> payload,
        Instant recordedAt
) {
}
