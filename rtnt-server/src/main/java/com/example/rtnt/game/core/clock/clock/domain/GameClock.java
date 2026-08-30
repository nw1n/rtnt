package com.example.rtnt.game.core.clock.clock.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameClock(long tick) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameClock initial() {
        return new GameClock(0);
    }

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameClock {
        if (tick < 0) {
            throw new IllegalArgumentException("tick must be >= 0");
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock advance() {
        return new GameClock(this.tick + 1);
    }
}
