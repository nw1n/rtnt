package com.example.rtnt.benchmark;

import com.example.rtnt.RtntDataTest;
import com.example.rtnt.game.core.loop.GameLoop;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Expensive manual timing run (wiped {@code rtnt-test} + {@code advance(10_000)}).
 * Not part of the normal suite: {@code ./gradlew test} excludes it, and the class stays
 * disabled unless {@code -Drtnt.benchmark=true}. Run with {@code ./gradlew :rtnt-server:benchmark}.
 */
@Tag("benchmark")
@EnabledIfSystemProperty(named = "rtnt.benchmark", matches = "true")
@RtntDataTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameLoopAdvanceBenchmarkTest {
    private static final Logger log = LoggerFactory.getLogger(GameLoopAdvanceBenchmarkTest.class);
    private static final int TICKS = 10_000;

    @Autowired
    private GameLoop gameLoop;

    @Test
    void timesAdvanceOnWipedTestDatabase() {
        assertEquals(0, this.gameLoop.get().tick());

        long start = System.nanoTime();
        this.gameLoop.advance(TICKS);
        double seconds = (System.nanoTime() - start) / 1e9;
        double ticksPerSecond = TICKS / seconds;

        String summary = "GameLoop.advance(%d) took %.3f s (%.0f ticks/s) on wiped rtnt-test".formatted(
                TICKS,
                seconds,
                ticksPerSecond
        );
        log.warn(summary);
        System.out.println(summary);

        assertEquals(TICKS, this.gameLoop.get().tick());
        assertTrue(seconds > 0);
    }
}
