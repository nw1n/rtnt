package com.example.rtnt.game.log.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameLogEvent(long tick, GameLogType type, String detail) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameLogEvent forTick(long tick) {
        return new GameLogEvent(tick, GameLogType.TICK, "Tick " + tick);
    }
}
