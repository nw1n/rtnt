package com.example.rtnt.game.ship.domain;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public record Journey(
        String id,
        String startIslandId,
        String targetIslandId,
        long departedTick,
        @Nullable Long arrivedTick,
        long estimatedArrivalTick,
        boolean active
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static Journey create(
            String startIslandId,
            String targetIslandId,
            long departedTick,
            long estimatedArrivalTick
    ) {
        return new Journey(
                UUID.randomUUID().toString(),
                startIslandId,
                targetIslandId,
                departedTick,
                null,
                estimatedArrivalTick,
                true
        );
    }

    public static Journey existing(
            String id,
            String startIslandId,
            String targetIslandId,
            long departedTick,
            @Nullable Long arrivedTick,
            long estimatedArrivalTick,
            boolean active
    ) {
        return new Journey(
                id,
                startIslandId,
                targetIslandId,
                departedTick,
                arrivedTick,
                estimatedArrivalTick,
                active
        );
    }

    /**
     * Inactive unless {@code active} was explicitly set to true (e.g. when persisting a new journey at departure).
     */
    public static boolean resolveActiveFromStorage(@Nullable Boolean stored) {
        return Boolean.TRUE.equals(stored);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Journey complete(long arrivedTick) {
        return new Journey(
                this.id,
                this.startIslandId,
                this.targetIslandId,
                this.departedTick,
                arrivedTick,
                this.estimatedArrivalTick,
                false
        );
    }
}
