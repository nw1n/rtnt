package com.example.rtnt.game.core.loop;

import com.example.rtnt.game.core.clock.clock.domain.ClockMode;

public record GameLoopStatus(long tick, ClockMode mode, boolean paused) {
}
