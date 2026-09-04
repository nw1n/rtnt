package com.example.rtnt.game.island.domain;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record Island(
    String id,
    String name,
    Footprint footprint
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static Island create(String name, Footprint footprint) {
        return new Island(UUID.randomUUID().toString(), name, footprint);
    }

    public static Island existing(String id, String name, Footprint footprint) {
        return new Island(id, name, footprint);
    }
}
