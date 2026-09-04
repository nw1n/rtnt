package com.example.rtnt.game.island.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandPlacement;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.persistence.IslandDocument;
import com.example.rtnt.game.island.persistence.IslandMongoRepository;
import com.example.rtnt.game.island.persistence.IslandStatusDocument;
import com.example.rtnt.game.island.persistence.IslandStatusMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IslandService {
    private static final Logger log = LoggerFactory.getLogger(IslandService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final IslandMongoRepository islandMongoRepository;
    private final IslandStatusMongoRepository islandStatusMongoRepository;
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
            IslandStatusMongoRepository islandStatusMongoRepository,
            @Value("${rtnt.startup.island-count:15}") int islandCount,
            @Value("${rtnt.map.width:2000}") int mapWidth,
            @Value("${rtnt.map.height:1000}") int mapHeight
    ) {
        this.islandMongoRepository = islandMongoRepository;
        this.islandStatusMongoRepository = islandStatusMongoRepository;
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

    public List<IslandStatus> listStatuses() {
        return this.islandStatusMongoRepository.findAll().stream()
                .map(IslandStatusDocument::toIslandStatus)
                .toList();
    }

    public List<Island> recreateAll() {
        this.islandStatusMongoRepository.deleteAll();
        this.islandMongoRepository.deleteAll();
        List<Island> placed = this.seed();
        log.info("Recreated {} islands", placed.size());
        return placed;
    }

    public void replaceAll(List<Island> islands, List<IslandStatus> statuses) {
        this.islandStatusMongoRepository.deleteAll();
        this.islandMongoRepository.deleteAll();
        this.islandMongoRepository.saveAll(islands.stream().map(IslandDocument::fromIsland).toList());
        this.islandStatusMongoRepository.saveAll(statuses.stream().map(IslandStatusDocument::from).toList());
        log.info("Replaced world with {} islands from snapshot", islands.size());
    }

    public void seedIfEmpty() {
        if (this.islandMongoRepository.count() > 0) {
            this.ensureStatuses();
            return;
        }
        log.info("Seeded {} islands", this.seed().size());
    }

    public void saveStatuses(List<IslandStatus> statuses) {
        if (statuses.isEmpty()) {
            return;
        }
        this.islandStatusMongoRepository.saveAll(statuses.stream().map(IslandStatusDocument::from).toList());
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
        this.islandStatusMongoRepository.saveAll(
                islands.stream()
                        .map(island -> IslandStatusDocument.from(IslandStatus.initial(island.id())))
                        .toList()
        );
        return islands;
    }

    private void ensureStatuses() {
        Set<String> existingIds = this.islandStatusMongoRepository.findAll().stream()
                .map(IslandStatusDocument::islandId)
                .collect(Collectors.toSet());
        List<IslandStatusDocument> missing = this.list().stream()
                .filter(island -> !existingIds.contains(island.id()))
                .map(island -> IslandStatusDocument.from(IslandStatus.initial(island.id())))
                .toList();
        if (!missing.isEmpty()) {
            this.islandStatusMongoRepository.saveAll(missing);
        }
    }
}
