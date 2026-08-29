package com.example.rtnt.game.clock.event;

import com.example.rtnt.game.clock.domain.GameClock;

public record ClockResumedEvent(GameClock clock) {
}
