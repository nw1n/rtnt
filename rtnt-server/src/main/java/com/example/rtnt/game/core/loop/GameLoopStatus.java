package com.example.rtnt.game.core.loop;

public record GameLoopStatus(long tick, ClockMode mode, boolean paused) {
}
