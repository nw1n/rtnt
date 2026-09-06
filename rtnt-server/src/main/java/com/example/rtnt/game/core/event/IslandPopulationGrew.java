package com.example.rtnt.game.core.event;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record IslandPopulationGrew(
        long tick,
        String islandId,
        long amount
) implements WorldEvent {
    public static final String TYPE = "IslandPopulationGrew";

    public IslandPopulationGrew {
        if (amount < 1) {
            throw new IllegalArgumentException("growth amount must be at least 1");
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
