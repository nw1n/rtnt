package com.example.rtnt.game.core.worldsnapshot.persistence;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.island.persistence.IslandDocument;
import com.example.rtnt.game.island.persistence.IslandStatusDocument;
import com.example.rtnt.game.ship.persistence.ShipDocument;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "world_snapshots")
@NullMarked
public record WorldSnapshotDocument(
        @Id long tick,
        List<IslandDocument> islands,
        List<IslandStatusDocument> islandStatuses,
        @Nullable List<ShipDocument> ships
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static WorldSnapshotDocument from(WorldSnapshot snapshot) {
        return new WorldSnapshotDocument(
                snapshot.tick(),
                snapshot.islands().stream().map(IslandDocument::fromIsland).toList(),
                snapshot.islandStatuses().stream().map(IslandStatusDocument::from).toList(),
                snapshot.ships().stream().map(ShipDocument::from).toList()
        );
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public WorldSnapshot toSnapshot() {
        List<ShipDocument> storedShips = this.ships;
        return new WorldSnapshot(
                this.tick,
                this.islands.stream().map(IslandDocument::toIsland).toList(),
                this.islandStatuses.stream().map(IslandStatusDocument::toIslandStatus).toList(),
                storedShips == null ? List.of() : storedShips.stream().map(ShipDocument::toShip).toList()
        );
    }
}
