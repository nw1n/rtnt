package com.example.rtnt.game.log.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameLogEvent(long tick, String type, String detail) {
    public static final String TYPE_TICK = "TICK";

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameLogEvent forTick(long tick) {
        return new GameLogEvent(tick, TYPE_TICK, "Tick " + tick);
    }
}
