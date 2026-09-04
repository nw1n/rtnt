package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShipService {
    private static final Logger log = LoggerFactory.getLogger(ShipService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final IslandService islandService;
    private final int shipCount;
    private final Map<String, Ship> shipsById = new LinkedHashMap<>();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ShipService(
            IslandService islandService,
            @Value("${rtnt.startup.ship-count:60}") int shipCount
    ) {
        this.islandService = islandService;
        this.shipCount = shipCount;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public synchronized List<Ship> list() {
        return List.copyOf(this.shipsById.values());
    }

    public synchronized Collection<Ship> view() {
        return Collections.unmodifiableCollection(this.shipsById.values());
    }

    public synchronized List<Ship> recreateAll() {
        this.shipsById.clear();
        List<Ship> seeded = this.seed();
        log.info("Recreated {} ships in memory", seeded.size());
        return seeded;
    }

    public synchronized void seedIfEmpty() {
        if (!this.shipsById.isEmpty()) {
            return;
        }
        log.info("Seeded {} ships in memory", this.seed().size());
    }

    public synchronized void replaceAll(List<Ship> ships) {
        this.shipsById.clear();
        for (Ship ship : ships) {
            this.shipsById.put(ship.getId(), ship);
        }
        log.info("Replaced in-memory world with {} ships from snapshot", ships.size());
    }

    public synchronized void save(List<Ship> ships) {
        for (Ship ship : ships) {
            this.shipsById.put(ship.getId(), ship);
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private List<Ship> seed() {
        var shipNames = ShipNamesLoader.load();
        List<Island> islands = this.islandService.list();
        List<Ship> ships = new ArrayList<>();
        for (int i = 0; i < this.shipCount; i++) {
            String islandId = islands.isEmpty() ? null : islands.get(i % islands.size()).id();
            Ship ship = Ship.create(shipNames.next(), islandId, null);
            ships.add(ship);
            this.shipsById.put(ship.getId(), ship);
        }
        return ships;
    }
}
