package com.example.rtnt.game.island.domain;

import com.example.rtnt.game.inventory.domain.Inventory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record IslandStatus(
        String islandId,
        long population,
        Inventory inventory
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static IslandStatus initial(String islandId) {
        return new IslandStatus(islandId, 0, Inventory.islandSeed());
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public IslandStatus grow(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("growth amount must be at least 1");
        }
        return new IslandStatus(this.islandId, this.population + amount, this.inventory);
    }

    public IslandStatus withInventory(Inventory inventory) {
        return new IslandStatus(this.islandId, this.population, inventory);
    }
}
