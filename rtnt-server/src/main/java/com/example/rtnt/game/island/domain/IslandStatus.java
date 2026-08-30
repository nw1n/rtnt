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
}
