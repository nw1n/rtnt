package com.example.rtnt.game.clock.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameClockTest {

    @Test
    void initialStartsAtTickZeroLiveAndUnpaused() {
        GameClock clock = GameClock.initial();

        assertEquals(0, clock.tick());
        assertEquals(ClockMode.LIVE, clock.mode());
        assertFalse(clock.paused());
    }

    @Test
    void advanceIncrementsTickAndKeepsModeAndPause() {
        GameClock clock = GameClock.initial().pause().withMode(ClockMode.BATCH).advance();

        assertEquals(1, clock.tick());
        assertEquals(ClockMode.BATCH, clock.mode());
        assertTrue(clock.paused());
    }

    @Test
    void rejectsNegativeTick() {
        assertThrows(IllegalArgumentException.class, () -> new GameClock(-1, ClockMode.LIVE, false));
    }
}
