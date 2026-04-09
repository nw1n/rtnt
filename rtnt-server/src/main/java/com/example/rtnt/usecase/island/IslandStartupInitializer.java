package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds islands on first run (empty store). Does not delete existing data.
 */
@Component
@Order(1)
public class IslandStartupInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IslandStartupInitializer.class);
    private static final int DEFAULT_ISLAND_COUNT = 15;

    private final IslandRepository islandRepository;
    private final IslandFactory islandFactory;
    private final int islandCount;

    public IslandStartupInitializer(
            IslandRepository islandRepository,
            IslandFactory islandFactory,
            @Value("${rtnt.startup.island-count:" + DEFAULT_ISLAND_COUNT + "}") int islandCount
    ) {
        if (islandCount < 1) {
            throw new IllegalArgumentException("rtnt.startup.island-count must be at least 1");
        }
        this.islandRepository = islandRepository;
        this.islandFactory = islandFactory;
        this.islandCount = islandCount;
    }

    private void seedIslandsIfEmpty() {
        long existing = this.islandRepository.count();
        if (existing > 0) {
            log.info("Skipping island seed: {} islands already in database", existing);
            return;
        }
        List<Island> placedIslands = new ArrayList<>();
        for (int i = 0; i < this.islandCount; i++) {
            Island island = this.islandFactory.createRandomizedWithCaribbeanName(placedIslands);
            Island savedIsland = this.islandRepository.save(island);
            placedIslands.add(savedIsland);
        }
        log.info("Startup seed completed: {} islands created", this.islandCount);
    }

    @Override
    public void run(String... args) {
        this.seedIslandsIfEmpty();
    }
}
