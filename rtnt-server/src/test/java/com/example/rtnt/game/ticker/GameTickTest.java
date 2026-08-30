package com.example.rtnt.game.ticker;

import com.example.rtnt.game.core.ticker.GameTick;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTickTest {

    @Test
    void initialStartsAtTickZero() {
        assertEquals(0, GameTick.initial().tick());
    }

    @Test
    void advanceIncrementsTick() {
        assertEquals(1, GameTick.initial().advance().tick());
    }

    @Test
    void rejectsNegativeTick() {
        assertThrows(IllegalArgumentException.class, () -> new GameTick(-1));
    }
}
