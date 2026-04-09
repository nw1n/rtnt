package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.StandardShip;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Use-case service for creating ships with configured naming strategies.
 */
@Component
public class ShipFactory {
    private final ShipNameProvider shipNameProvider;
    private final IslandRepository islandRepository;

    public ShipFactory(ShipNameProvider shipNameProvider, IslandRepository islandRepository) {
        this.shipNameProvider = shipNameProvider;
        this.islandRepository = islandRepository;
    }

    /**
     * Create a new ship with a name selected from the ship names.
     * Uses the next name in sequence.
     *
     * @return a new Ship with a ship name
     */
    public Ship createWithCaribbeanName() {
        String name = this.shipNameProvider.getNextName();
        return StandardShip.create(name, this.randomIslandId(), null, null, this.defaultShipInventory());
    }

    /**
     * Create a new ship with a random name from the ship names.
     *
     * @return a new Ship with a random ship name
     */
    public Ship createWithRandomCaribbeanName() {
        String name = this.shipNameProvider.getRandomName();
        return StandardShip.create(name, this.randomIslandId(), null, null, this.defaultShipInventory());
    }

    /**
     * Create a new ship with a specific name.
     *
     * @param name the name for the ship
     * @return a new Ship with the specified name
     */
    public Ship createWithName(String name) {
        return StandardShip.create(name, this.randomIslandId(), null, null, this.defaultShipInventory());
    }

    /**
     * Create a new ship with a specific name and controlling player.
     *
     * @param name the name for the ship
     * @param playerId the player controlling the ship
     * @return a new Ship with the specified name and player assignment
     */
    public Ship createWithNameForPlayer(String name, String playerId) {
        return StandardShip.create(name, this.randomIslandId(), null, playerId, this.defaultShipInventory());
    }

    private Inventory defaultShipInventory() {
        return Inventory.of(Map.of(
                GoodType.GOLD, 1_000,
                GoodType.RUM, 5,
                GoodType.SUGAR, 4,
                GoodType.SPICES, 3,
                GoodType.TOBACCO, 6
        ));
    }

    private String randomIslandId() {
        List<Island> islands = this.islandRepository.findAll();
        if (islands.isEmpty()) {
            throw new IllegalStateException("Cannot create ship without islands");
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(islands.size());
        return islands.get(randomIndex).getId();
    }
}
