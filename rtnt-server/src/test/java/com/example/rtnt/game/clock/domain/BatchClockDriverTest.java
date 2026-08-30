package com.example.rtnt.game.clock.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchClockDriverTest {

    @Test
    void runAdvancesExactlyNTicks() {
        GameClock clock = new BatchClockDriver().run(GameClock.initial(), 5);

        assertEquals(5, clock.tick());
        assertEquals(ClockMode.LIVE, clock.mode());
    }

    @Test
    void runAdvancesManyTicksFromCurrent() {
        GameClock start = new GameClock(1_000, ClockMode.BATCH, false);

        GameClock clock = new BatchClockDriver().run(start, 100_000);

        assertEquals(101_000, clock.tick());
        assertEquals(ClockMode.BATCH, clock.mode());
    }

    @Test
    void runRejectsNonPositiveTicks() {
        BatchClockDriver driver = new BatchClockDriver();

        assertThrows(IllegalArgumentException.class, () -> driver.run(GameClock.initial(), 0));
    }
}
