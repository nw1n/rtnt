package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class TradeWithIslandUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeWithIslandUseCase.class);

    private final ShipRepository shipRepository;
    private final IslandRepository islandRepository;
    private final TradeEventPublisher tradeEventPublisher;

    public TradeWithIslandUseCase(
            ShipRepository shipRepository,
            IslandRepository islandRepository,
            TradeEventPublisher tradeEventPublisher
    ) {
        this.shipRepository = shipRepository;
        this.islandRepository = islandRepository;
        this.tradeEventPublisher = tradeEventPublisher;
    }

    public Ship buyFromIsland(String shipId, GoodType goodType, int amount) {
        this.validateTradeInput(goodType, amount);

        Ship ship = this.loadShip(shipId);
        Island island = this.loadAnchoredIsland(ship);
        int unitPrice = island.getTradePrices().getPrice(goodType);
        int totalPrice = this.getTotalPrice(unitPrice, amount);

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
        Ship updatedShip = this.shipRepository.save(ship);
        this.publishTradeEvent(new TradeEvent(
                Instant.now(),
                TradeType.BUY_FROM_ISLAND,
                updatedShip.getId(),
                updatedShip.getName(),
                island.getId(),
                island.getName(),
                goodType,
                amount,
                unitPrice,
                totalPrice
        ));
        return updatedShip;
    }

    public Ship sellToIsland(String shipId, GoodType goodType, int amount) {
        this.validateTradeInput(goodType, amount);

        Ship ship = this.loadShip(shipId);
        Island island = this.loadAnchoredIsland(ship);
        int unitPrice = island.getTradePrices().getPrice(goodType);
        int totalPrice = this.getTotalPrice(unitPrice, amount);

        ship.getInventory().removeAmount(goodType, amount);
        island.getInventory().addAmount(goodType, amount);

        island.getInventory().removeAmount(GoodType.GOLD, totalPrice);
        ship.getInventory().addAmount(GoodType.GOLD, totalPrice);

        this.islandRepository.save(island);
        Ship updatedShip = this.shipRepository.save(ship);
        this.publishTradeEvent(new TradeEvent(
                Instant.now(),
                TradeType.SELL_TO_ISLAND,
                updatedShip.getId(),
                updatedShip.getName(),
                island.getId(),
                island.getName(),
                goodType,
                amount,
                unitPrice,
                totalPrice
        ));
        return updatedShip;
    }

    private void publishTradeEvent(TradeEvent event) {
        LOGGER.info(
                "Publishing trade event: type={}, shipId={}, shipName={}, islandId={}, islandName={}, goodType={}, amount={}, unitPrice={}, totalPrice={}",
                event.tradeType(),
                event.shipId(),
                event.shipName(),
                event.islandId(),
                event.islandName(),
                event.goodType(),
                event.amount(),
                event.unitPrice(),
                event.totalPrice()
        );
        Thread.ofVirtual().name("trade-event-publisher").start(() -> {
            try {
                this.tradeEventPublisher.publish(event);
            } catch (Exception ex) {
                LOGGER.error("Failed to publish trade event (trade still succeeded): {}", ex.getMessage(), ex);
            }
        });
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

    private int getTotalPrice(int unitPrice, int amount) {
        try {
            return Math.multiplyExact(unitPrice, amount);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Trade amount is too large", ex);
        }
    }
}
