package com.example.rtnt.game.island.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record IslandStatus(
        String islandId,
        long population
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static IslandStatus initial(String islandId) {
        return new IslandStatus(islandId, 0);
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
        return new IslandStatus(this.islandId, this.population + amount);
    }
}
