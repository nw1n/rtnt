package com.example.rtnt.game.core.event;

import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface WorldEvent permits IslandCreated, IslandPopulationGrew, WorldCleared {
    String type();

    long tick();
}
