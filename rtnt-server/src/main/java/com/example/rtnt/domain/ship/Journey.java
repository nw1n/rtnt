package com.example.rtnt.domain.ship;

import java.time.Instant;

public record Journey(
        String id,
        String shipId,
        String startIslandId,
        String targetIslandId,
        Instant departed,
        Instant arrived,
        Instant estimatedArrival,
        boolean active
) {
    /**
     * Inactive unless {@code active} was explicitly set to true (e.g. when persisting a new journey at departure).
     */
    public static boolean resolveActiveFromStorage(Boolean stored) {
        return Boolean.TRUE.equals(stored);
    }
}
