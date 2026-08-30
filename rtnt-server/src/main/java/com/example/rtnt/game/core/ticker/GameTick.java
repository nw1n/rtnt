package com.example.rtnt.game.core.ticker;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameTick(long tick) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameTick initial() {
        return new GameTick(0);
    }

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameTick {
        if (tick < 0) {
            throw new IllegalArgumentException("tick must be >= 0");
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameTick advance() {
        return new GameTick(this.tick + 1);
    }
}
