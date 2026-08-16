package com.example.rtnt.service;

import com.example.rtnt.domain.island.Footprint;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.persistence.island.IslandDocument;
import com.example.rtnt.persistence.island.IslandMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IslandService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(IslandService.class);
    private static final int MIN_WIDTH = 20;
    private static final int MAX_WIDTH = 100;
    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 100;
    private static final int MAX_ATTEMPTS = 1_000;
    private static final int MIN_DISTANCE = 10;
    private static final int MAX_X = 2_000;
    private static final int MAX_Y = 1_000;

    private final IslandMongoRepository islandMongoRepository;
    private final IslandNames islandNames;
    private final int islandCount;

    public IslandService(
            IslandMongoRepository islandMongoRepository,
            IslandNames islandNames,
            @Value("${rtnt.startup.island-count:15}") int islandCount
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

    Island place(String name, List<Island> existing) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int width = randomInRange(MIN_WIDTH, MAX_WIDTH);
            int length = randomInRange(MIN_LENGTH, MAX_LENGTH);
            int x = ThreadLocalRandom.current().nextInt(MAX_X - width + 1);
            int y = ThreadLocalRandom.current().nextInt(MAX_Y - length + 1);
            Island candidate = Island.create(name, Footprint.create(x, y, width, length));
            boolean blocked = existing.stream()
                    .anyMatch(island -> candidate.footprint().overlapsOrTooClose(island.footprint(), MIN_DISTANCE));
            if (!blocked) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not place island after " + MAX_ATTEMPTS + " attempts");
    }

    public List<Island> recreateAll() {
        this.islandMongoRepository.deleteAll();
        this.islandNames.reset();
        List<Island> placed = this.seed();
        log.info("Recreated {} islands", placed.size());
        return placed;
    }

    @Override
    public void run(String... args) {
        if (List.of(args).contains("recreate-islands")) {
            return;
        }
        if (this.islandMongoRepository.count() > 0) {
            return;
        }
        log.info("Seeded {} islands", this.seed().size());
    }

    private List<Island> seed() {
        List<Island> placed = new ArrayList<>();
        for (int i = 0; i < this.islandCount; i++) {
            Island island = this.place(this.islandNames.next(), placed);
            placed.add(this.islandMongoRepository.save(IslandDocument.fromIsland(island)).toIsland());
        }
        return placed;
    }

    private static int randomInRange(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }
}
