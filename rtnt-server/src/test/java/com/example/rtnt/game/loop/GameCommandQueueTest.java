package com.example.rtnt.game.loop;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameCommandQueueTest {

    @Test
    void drainReturnsCommandsForTickAndRemovesThem() {
        GameCommandQueue queue = new GameCommandQueue();
        queue.enqueue(3, new GameCommand("a"));
        queue.enqueue(3, new GameCommand("b"));
        queue.enqueue(4, new GameCommand("c"));

        List<GameCommand> drained = queue.drain(3);

        assertEquals(List.of(new GameCommand("a"), new GameCommand("b")), drained);
        assertEquals(List.of(), queue.drain(3));
        assertEquals(List.of(new GameCommand("c")), queue.drain(4));
    }
}
