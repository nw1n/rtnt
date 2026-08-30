package com.example.rtnt.game.loop.persistence;

import com.example.rtnt.game.island.persistence.IslandDocument;
import com.example.rtnt.game.loop.WorldSnapshot;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "world_snapshots")
@NullMarked
public record WorldSnapshotDocument(
        @Id long tick,
        List<IslandDocument> islands
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static WorldSnapshotDocument from(WorldSnapshot snapshot) {
        return new WorldSnapshotDocument(
                snapshot.tick(),
                snapshot.islands().stream().map(IslandDocument::fromIsland).toList()
        );
    }
}
