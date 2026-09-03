package com.example.rtnt.game.ship.domain;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@NullMarked
public record Journey(
        String id,
        String shipId,
        String startIslandId,
        String targetIslandId,
        Instant departed,
        @Nullable Instant arrived,
        Instant estimatedArrival,
        boolean active
) {
    public static boolean resolveActiveFromStorage(@Nullable Boolean stored) {
        return Boolean.TRUE.equals(stored);
    }
}
