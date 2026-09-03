package com.example.rtnt.game.ship.domain;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

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
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static Journey create(
            String shipId,
            String startIslandId,
            String targetIslandId,
            Instant departed,
            Instant estimatedArrival
    ) {
        return new Journey(
                UUID.randomUUID().toString(),
                shipId,
                startIslandId,
                targetIslandId,
                departed,
                null,
                estimatedArrival,
                true
        );
    }

    public static Journey existing(
            String id,
            String shipId,
            String startIslandId,
            String targetIslandId,
            Instant departed,
            @Nullable Instant arrived,
            Instant estimatedArrival,
            boolean active
    ) {
        return new Journey(
                id,
                shipId,
                startIslandId,
                targetIslandId,
                departed,
                arrived,
                estimatedArrival,
                active
        );
    }

    /**
     * Inactive unless {@code active} was explicitly set to true (e.g. when persisting a new journey at departure).
     */
    public static boolean resolveActiveFromStorage(@Nullable Boolean stored) {
        return Boolean.TRUE.equals(stored);
    }
}
