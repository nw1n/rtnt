package com.example.rtnt.game.loop.persistence;

import com.example.rtnt.game.clock.domain.GameClock;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "world_snapshots")
@NullMarked
public record WorldSnapshotDocument(
        @Id long tick
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static WorldSnapshotDocument from(GameClock clock) {
        return new WorldSnapshotDocument(clock.tick());
    }
}
