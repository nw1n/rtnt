package com.example.rtnt.service;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.persistence.island.IslandDocument;
import com.example.rtnt.persistence.island.IslandMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IslandService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(IslandService.class);
    private static final int DEFAULT_ISLAND_COUNT = 15;
    private static final int MIN_WIDTH = 20;
    private static final int MAX_WIDTH = 100;
    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 100;
    private static final int MAX_ATTEMPTS = 1_000;
    private static final int MIN_DISTANCE = 10;
    private static final int MAX_X_COORDINATE = 2_000;
    private static final int MAX_Y_COORDINATE = 1_000;

    private final IslandMongoRepository islandMongoRepository;
    private final IslandNames islandNames;
    private final int islandCount;

    public IslandService(
            IslandMongoRepository islandMongoRepository,
            IslandNames islandNames,
            @Value("${rtnt.startup.island-count:" + DEFAULT_ISLAND_COUNT + "}") int islandCount
    ) {
        if (islandCount < 1) {
            throw new IllegalArgumentException("rtnt.startup.island-count must be at least 1");
        }
        this.islandMongoRepository = islandMongoRepository;
        this.islandNames = islandNames;
        this.islandCount = islandCount;
    }

    public List<Island> list() {
        return this.islandMongoRepository.findAll().stream()
                .map(IslandDocument::toIsland)
                .toList();
    }

    public Island createAndSave(String requestedName) {
        List<Island> existing = this.list();
        String name = requestedName == null || requestedName.isBlank()
                ? this.islandNames.next()
                : requestedName.trim();
        Island created = this.createRandomized(name, existing);
        return this.islandMongoRepository.save(IslandDocument.fromIsland(created)).toIsland();
    }

    public Island createRandomized(String name, List<Island> existingIslands) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            int width = randomInRange(MIN_WIDTH, MAX_WIDTH);
            int length = randomInRange(MIN_LENGTH, MAX_LENGTH);
            int x = ThreadLocalRandom.current().nextInt(MAX_X_COORDINATE - width + 1);
            int y = ThreadLocalRandom.current().nextInt(MAX_Y_COORDINATE - length + 1);
            Island candidate = Island.create(
                    name,
                    Footprint.create(x, y, width, length),
                    defaultInventory(),
                    defaultTradePrices()
            );
            if (!overlapsOrTooClose(candidate, existingIslands)) {
                return candidate;
            }
            attempts++;
        }
        throw new IllegalStateException("Could not find a non-overlapping position after " + MAX_ATTEMPTS + " attempts");
    }

    @Override
    public void run(String... args) {
        long existing = this.islandMongoRepository.count();
        if (existing > 0) {
            log.info("Skipping island seed: {} islands already in database", existing);
            return;
        }
        List<Island> placed = new ArrayList<>();
        for (int i = 0; i < this.islandCount; i++) {
            Island island = this.createRandomized(this.islandNames.next(), placed);
            Island saved = this.islandMongoRepository.save(IslandDocument.fromIsland(island)).toIsland();
            placed.add(saved);
        }
        log.info("Startup seed completed: {} islands created", this.islandCount);
    }

    public static Inventory defaultInventory() {
        return Inventory.of(Map.of(
                GoodType.GOLD, 100,
                GoodType.RUM, 10,
                GoodType.SUGAR, 10,
                GoodType.SPICES, 10,
                GoodType.TOBACCO, 10
        ));
    }

    public static TradePriceList defaultTradePrices() {
        return TradePriceList.of(Map.of(
                GoodType.RUM, 3,
                GoodType.SUGAR, 2,
                GoodType.SPICES, 4,
                GoodType.TOBACCO, 5
        ));
    }

    private static int randomInRange(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static boolean overlapsOrTooClose(Island candidate, List<Island> existingIslands) {
        for (Island existing : existingIslands) {
            if (candidate.getFootprint().overlapsOrTooClose(existing.getFootprint(), MIN_DISTANCE)) {
                return true;
            }
        }
        return false;
    }
}
