package com.example.rtnt.service.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.persistence.island.IslandDocument;
import com.example.rtnt.persistence.island.IslandMongoRepository;
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
            @Value("${rtnt.startup.island-count:15}") int islandCount,
            @Value("${rtnt.map.width:2000}") int mapWidth,
            @Value("${rtnt.map.height:1000}") int mapHeight
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
        var islandNames = new IslandNames();
        var islandPlacement = new IslandPlacement(this.mapWidth, this.mapHeight);
        List<Island> islands = new ArrayList<>();
        
        for (int i = 0; i < this.islandCount; i++) {
            var newIsland = islandPlacement.placeIsland(islandNames.next(), islands)
            islands.add(newIsland);
        }

        var dataToSave = islands.stream().map(IslandDocument::fromIsland).toList();
        this.islandMongoRepository.saveAll(dataToSave);
        return islands;
    }
}
