package com.example.rtnt.game.island.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandPlacement;
import com.example.rtnt.game.island.domain.IslandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IslandService {
    private static final Logger log = LoggerFactory.getLogger(IslandService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final int islandCount;
    private final int mapWidth;
    private final int mapHeight;
    private final Map<String, Island> islandsById = new LinkedHashMap<>();
    private final Map<String, IslandStatus> statusesById = new LinkedHashMap<>();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public IslandService(
            @Value("${rtnt.startup.island-count:15}") int islandCount,
            @Value("${rtnt.map.width:2000}") int mapWidth,
            @Value("${rtnt.map.height:1000}") int mapHeight
    ) {
        this.islandCount = islandCount;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public synchronized List<Island> list() {
        return List.copyOf(this.islandsById.values());
    }

    public synchronized List<IslandStatus> listStatuses() {
        return List.copyOf(this.statusesById.values());
    }

    public synchronized List<Island> recreateAll() {
        this.islandsById.clear();
        this.statusesById.clear();
        List<Island> placed = this.seed();
        log.info("Recreated {} islands in memory", placed.size());
        return placed;
    }

    public synchronized void replaceAll(List<Island> islands, List<IslandStatus> statuses) {
        this.islandsById.clear();
        this.statusesById.clear();
        for (Island island : islands) {
            this.islandsById.put(island.id(), island);
        }
        for (IslandStatus status : statuses) {
            this.statusesById.put(status.islandId(), status);
        }
        log.info("Replaced in-memory world with {} islands from snapshot", islands.size());
    }

    public synchronized void seedIfEmpty() {
        if (!this.islandsById.isEmpty()) {
            return;
        }
        log.info("Seeded {} islands in memory", this.seed().size());
    }

    public synchronized void saveStatuses(List<IslandStatus> statuses) {
        for (IslandStatus status : statuses) {
            this.statusesById.put(status.islandId(), status);
        }
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
            this.islandsById.put(newIsland.id(), newIsland);
            this.statusesById.put(newIsland.id(), IslandStatus.initial(newIsland.id()));
        }
        return islands;
    }
}
