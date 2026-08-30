package com.example.rtnt.game.loop;

import com.example.rtnt.game.core.loop.GameCommand;
import com.example.rtnt.game.core.loop.GameCommandQueue;
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

    @Test
    void clearRemovesQueuedCommands() {
        GameCommandQueue queue = new GameCommandQueue();
        queue.enqueue(1, new GameCommand("a"));
        queue.enqueue(2, new GameCommand("b"));

        queue.clear();

        assertEquals(List.of(), queue.drain(1));
        assertEquals(List.of(), queue.drain(2));
    }
}
