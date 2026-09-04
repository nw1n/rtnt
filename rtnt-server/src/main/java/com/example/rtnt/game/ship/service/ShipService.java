package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.persistence.ShipDocument;
import com.example.rtnt.game.ship.persistence.ShipMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShipService {
    private static final Logger log = LoggerFactory.getLogger(ShipService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final ShipMongoRepository shipMongoRepository;
    private final IslandService islandService;
    private final int shipCount;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ShipService(
            ShipMongoRepository shipMongoRepository,
            IslandService islandService,
            @Value("${rtnt.startup.ship-count:60}") int shipCount
    ) {
        this.shipMongoRepository = shipMongoRepository;
        this.islandService = islandService;
        this.shipCount = shipCount;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public List<Ship> list() {
        return this.shipMongoRepository.findAll().stream()
                .map(ShipDocument::toShip)
                .toList();
    }

    public List<Ship> recreateAll() {
        this.shipMongoRepository.deleteAll();
        List<Ship> seeded = this.seed();
        log.info("Recreated {} ships", seeded.size());
        return seeded;
    }

    public void seedIfEmpty() {
        if (this.shipMongoRepository.count() > 0) {
            return;
        }
        log.info("Seeded {} ships", this.seed().size());
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
            ships.add(Ship.create(shipNames.next(), islandId, null));
        }
        this.shipMongoRepository.saveAll(ships.stream().map(ShipDocument::from).toList());
        return ships;
    }
}
