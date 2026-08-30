package com.example.rtnt.game.core.loop;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@NullMarked
public class GameCommandQueue {
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<GameCommand>> byTick = new ConcurrentHashMap<>();

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public void enqueue(long tick, GameCommand command) {
        this.byTick.computeIfAbsent(tick, key -> new ConcurrentLinkedQueue<>()).add(command);
    }

    public List<GameCommand> drain(long tick) {
        ConcurrentLinkedQueue<GameCommand> queued = this.byTick.remove(tick);
        if (queued == null) {
            return List.of();
        }
        return List.copyOf(queued);
    }

    public void clear() {
        this.byTick.clear();
    }
}
