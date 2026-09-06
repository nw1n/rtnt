package com.example.rtnt.game.core.event;

import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public interface EventStore {
    String WORLD_STREAM = "world";

    void append(List<WorldEvent> events);

    List<WorldEvent> readAll();

    List<WorldEvent> readUpToTick(long tick);

    void deleteAfterTick(long tick);

    boolean isEmpty();
}
