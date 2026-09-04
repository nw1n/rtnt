package com.example.rtnt.game.trade.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrivalTradeTest {

    @Test
    void maxBuyIsLimitedByHoldStockAndGold() {
        Inventory ship = Inventory.of(Map.of(GoodType.GOLD, 9, GoodType.RUM, 98));
        Inventory island = Inventory.of(Map.of(GoodType.RUM, 50));
        assertEquals(2, ArrivalTrade.maxBuyAmount(100, ship, island, GoodType.RUM, 3));
    }

    @Test
    void maxSellIsLimitedByShipStockAndIslandGold() {
        Inventory ship = Inventory.of(Map.of(GoodType.RUM, 8));
        Inventory island = Inventory.of(Map.of(GoodType.GOLD, 10));
        assertEquals(3, ArrivalTrade.maxSellAmount(ship, island, GoodType.RUM, 3));
    }

    @Test
    void executeConservesGoodsAndGold() {
        Island island = Island.create("Jamaica", new Footprint(0, 0, 10, 10));
        IslandStatus status = IslandStatus.initial(island.id());
        Ship ship = Ship.create("Black Pearl", island.id(), null);
        Inventory shipBefore = ship.getInventory();
        Inventory islandBefore = status.inventory();

        TradeResult result = new ArrivalTrade(new Random(1)).execute(ship, island, status, 40);

        assertFalse(result.events().isEmpty());
        assertEquals(
                shipBefore.getAmount(GoodType.GOLD) + islandBefore.getAmount(GoodType.GOLD),
                result.ship().getInventory().getAmount(GoodType.GOLD)
                        + result.islandStatus().inventory().getAmount(GoodType.GOLD)
        );
        for (GoodType good : GoodType.tradeableGoods()) {
            assertEquals(
                    shipBefore.getAmount(good) + islandBefore.getAmount(good),
                    result.ship().getInventory().getAmount(good)
                            + result.islandStatus().inventory().getAmount(good)
            );
        }
        int expectedGoldDelta = 0;
        for (TradeEvent event : result.events()) {
            assertTrue(event.amount() >= 1);
            assertEquals(event.unitPrice() * event.amount(), event.totalPrice());
            assertEquals(island.tradePrices().getPrice(event.goodType()), event.unitPrice());
            if (event.tradeType().name().startsWith("SELL")) {
                expectedGoldDelta += event.totalPrice();
            } else {
                expectedGoldDelta -= event.totalPrice();
            }
        }
        assertEquals(
                shipBefore.getAmount(GoodType.GOLD) + expectedGoldDelta,
                result.ship().getInventory().getAmount(GoodType.GOLD)
        );
    }

    @Test
    void executeDoesNothingWhenNothingIsAffordable() {
        Island island = Island.create("Jamaica", new Footprint(0, 0, 10, 10));
        IslandStatus status = new IslandStatus(island.id(), 0, Inventory.empty());
        Ship ship = Ship.create("Black Pearl", island.id(), null, null, Inventory.empty());

        TradeResult result = new ArrivalTrade(new Random(1)).execute(ship, island, status, 40);

        assertTrue(result.events().isEmpty());
        assertEquals(Inventory.empty(), result.ship().getInventory());
        assertEquals(Inventory.empty(), result.islandStatus().inventory());
    }
}
