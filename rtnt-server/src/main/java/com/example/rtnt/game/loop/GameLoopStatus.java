package com.example.rtnt.game.loop;

import com.example.rtnt.game.clock.domain.ClockMode;

public record GameLoopStatus(long tick, ClockMode mode, boolean paused) {
}
