package com.example.rtnt.game.island.persistence;

import com.example.rtnt.game.inventory.persistence.InventoryDocument;
import com.example.rtnt.game.island.domain.IslandStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "island_status")
@NullMarked
public record IslandStatusDocument(
        @Id String islandId,
        long population,
        @Nullable InventoryDocument inventory
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static IslandStatusDocument from(IslandStatus status) {
        return new IslandStatusDocument(
                status.islandId(),
                status.population(),
                InventoryDocument.from(status.inventory())
        );
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public IslandStatus toIslandStatus() {
        return new IslandStatus(this.islandId, this.population, InventoryDocument.toInventory(this.inventory));
    }
}
