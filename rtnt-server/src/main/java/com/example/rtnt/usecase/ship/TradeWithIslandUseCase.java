package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class TradeWithIslandUseCase {

    private final ShipRepository shipRepository;
    private final IslandRepository islandRepository;

    public TradeWithIslandUseCase(ShipRepository shipRepository, IslandRepository islandRepository) {
        this.shipRepository = shipRepository;
        this.islandRepository = islandRepository;
    }

    public Ship buyFromIsland(String shipId, GoodType goodType, int amount) {
        this.validateTradeInput(goodType, amount);

        Ship ship = this.loadShip(shipId);
        Island island = this.loadAnchoredIsland(ship);
        int totalPrice = this.getTotalPrice(island, goodType, amount);

        int holdRoom = ship.getCargoCapacity() - ship.getInventory().sumTradeableGoods();
        if (amount > holdRoom) {
            throw new IllegalArgumentException(
                    "Cannot buy " + amount + " units; ship hold fits " + holdRoom + " more (capacity "
                            + ship.getCargoCapacity() + ")"
            );
        }

        ship.getInventory().removeAmount(GoodType.GOLD, totalPrice);
        island.getInventory().addAmount(GoodType.GOLD, totalPrice);

        island.getInventory().removeAmount(goodType, amount);
        ship.getInventory().addAmount(goodType, amount);

        this.islandRepository.save(island);
        return this.shipRepository.save(ship);
    }

    public Ship sellToIsland(String shipId, GoodType goodType, int amount) {
        this.validateTradeInput(goodType, amount);

        Ship ship = this.loadShip(shipId);
        Island island = this.loadAnchoredIsland(ship);
        int totalPrice = this.getTotalPrice(island, goodType, amount);

        ship.getInventory().removeAmount(goodType, amount);
        island.getInventory().addAmount(goodType, amount);

        island.getInventory().removeAmount(GoodType.GOLD, totalPrice);
        ship.getInventory().addAmount(GoodType.GOLD, totalPrice);

        this.islandRepository.save(island);
        return this.shipRepository.save(ship);
    }

    private void validateTradeInput(GoodType goodType, int amount) {
        if (goodType == null) {
            throw new IllegalArgumentException("Good type is required");
        }
        if (!goodType.isTradeable()) {
            throw new IllegalArgumentException("Cannot trade GOLD as a cargo good");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Trade amount must be greater than 0");
        }
    }

    private Ship loadShip(String shipId) {
        return this.shipRepository.findById(shipId)
                .orElseThrow(() -> new NoSuchElementException("Ship not found: " + shipId));
    }

    private Island loadAnchoredIsland(Ship ship) {
        String islandId = ship.getIslandId();
        if (islandId == null || islandId.isBlank()) {
            throw new IllegalStateException("Ship must be anchored at an island to trade");
        }
        return this.islandRepository.findById(islandId)
                .orElseThrow(() -> new NoSuchElementException("Anchored island not found: " + islandId));
    }

    private int getTotalPrice(Island island, GoodType goodType, int amount) {
        int unitPrice = island.getTradePrices().getPrice(goodType);
        try {
            return Math.multiplyExact(unitPrice, amount);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Trade amount is too large", ex);
        }
    }
}
