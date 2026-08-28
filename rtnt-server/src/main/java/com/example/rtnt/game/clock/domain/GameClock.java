package com.example.rtnt.game.clock.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameClock(long tick, ClockMode mode, boolean paused) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameClock initial() {
        return new GameClock(0, ClockMode.LIVE, false);
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
        return new GameClock(this.tick + 1, this.mode, this.paused);
    }

    public GameClock pause() {
        return new GameClock(this.tick, this.mode, true);
    }

    public GameClock resume() {
        return new GameClock(this.tick, this.mode, false);
    }

    public GameClock withMode(ClockMode mode) {
        return new GameClock(this.tick, mode, this.paused);
    }
}
