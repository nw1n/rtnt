package com.example.rtnt.game.island.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandPlacement;
import com.example.rtnt.game.island.persistence.IslandDocument;
import com.example.rtnt.game.island.persistence.IslandMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IslandService {
    private static final Logger log = LoggerFactory.getLogger(IslandService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final IslandMongoRepository islandMongoRepository;
    private final int islandCount;
    private final int mapWidth;
    private final int mapHeight;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public IslandService(
            IslandMongoRepository islandMongoRepository,
            @Value("${rtnt.startup.island-count}") int islandCount,
            @Value("${rtnt.map.width}") int mapWidth,
            @Value("${rtnt.map.height}") int mapHeight
    ) {
        this.islandMongoRepository = islandMongoRepository;
        this.islandCount = islandCount;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public List<Island> list() {
        return this.islandMongoRepository.findAll().stream()
                .map(IslandDocument::toIsland)
                .toList();
    }

    public List<Island> recreateAll() {
        this.islandMongoRepository.deleteAll();
        List<Island> placed = this.seed();
        log.info("Recreated {} islands", placed.size());
        return placed;
    }

    public void seedIfEmpty() {
        if (this.islandMongoRepository.count() > 0) {
            return;
        }
        log.info("Seeded {} islands", this.seed().size());
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private List<Island> seed() {
        var islandNames = IslandNamesLoader.load();
        var islandPlacement = new IslandPlacement(this.mapWidth, this.mapHeight);
        List<Island> islands = new ArrayList<>();

        for (int i = 0; i < this.islandCount; i++) {
            var newIsland = islandPlacement.placeIsland(islandNames.next(), islands);
            islands.add(newIsland);
        }

        var dataToSave = islands.stream().map(IslandDocument::fromIsland).toList();
        this.islandMongoRepository.saveAll(dataToSave);
        return islands;
    }
}
