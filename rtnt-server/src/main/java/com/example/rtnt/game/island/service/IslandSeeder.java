package com.example.rtnt.game.island.service;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * On startup, seed islands if none exist.
 */
@Component
@Order(1)
public class IslandSeeder implements ApplicationRunner {
    private final IslandService islandService;

    public IslandSeeder(IslandService islandService) {
        this.islandService = islandService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        this.islandService.seedIfEmpty();
    }
}
