package com.example.rtnt.game.clock.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BatchClockDriver {
    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock run(GameClock start, int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        GameClock clock = start;
        for (int i = 0; i < ticks; i++) {
            clock = clock.advance();
        }
        return clock;
    }
}
