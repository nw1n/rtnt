package com.example.rtnt.game.clock.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameClockTest {

    @Test
    void initialStartsAtTickZero() {
        assertEquals(0, GameClock.initial().tick());
    }

    @Test
    void advanceIncrementsTick() {
        assertEquals(1, GameClock.initial().advance().tick());
    }

    @Test
    void rejectsNegativeTick() {
        assertThrows(IllegalArgumentException.class, () -> new GameClock(-1));
    }
}
