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
    void runRejectsNonPositiveTicks() {
        BatchClockDriver driver = new BatchClockDriver();

        assertThrows(IllegalArgumentException.class, () -> driver.run(GameClock.initial(), 0));
    }
}
