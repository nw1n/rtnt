package com.example.rtnt.game.trade.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.domain.TradePriceList;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeResult;
import com.example.rtnt.game.trade.domain.TradeType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrivalTradeTest {

    @Test
    void sellProbabilityBiasesTowardHighPricesButNeverLocks() {
        assertEquals(0.5, ArrivalTrade.sellProbability(3, 3));
        assertTrue(ArrivalTrade.sellProbability(6, 3) > 0.5);
        assertTrue(ArrivalTrade.sellProbability(1, 3) < 0.5);
        assertTrue(ArrivalTrade.sellProbability(1, 20) >= 0.2);
        assertTrue(ArrivalTrade.sellProbability(1, 20) < 0.5);
        assertEquals(0.8, ArrivalTrade.sellProbability(20, 1));
    }

    @Test
    void buyTaxIsFivePercentOfUnitPriceRoundedDown() {
        assertEquals(0, ArrivalTrade.buyTax(18));
        assertEquals(0, ArrivalTrade.buyTax(19));
        assertEquals(1, ArrivalTrade.buyTax(20));
        assertEquals(1, ArrivalTrade.buyTax(39));
        assertEquals(2, ArrivalTrade.buyTax(40));
    }

    @Test
    void maxBuyIsLimitedByHoldStockAndGoldIncludingTax() {
        Inventory ship = Inventory.of(Map.of(GoodType.GOLD, 9, GoodType.RUM, 98));
        Inventory island = Inventory.of(Map.of(GoodType.RUM, 50));
        assertEquals(2, ArrivalTrade.maxBuyAmount(100, ship, island, GoodType.RUM, 3));
        Inventory tightGold = Inventory.of(Map.of(GoodType.GOLD, 100));
        assertEquals(1, ArrivalTrade.maxBuyAmount(100, tightGold, island, GoodType.RUM, 50));
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
            int goodsCost = event.unitPrice() * event.amount();
            int tax = event.tradeType().name().startsWith("SELL")
                    ? 0
                    : ArrivalTrade.buyTax(event.unitPrice()) * event.amount();
            assertEquals(goodsCost + tax, event.totalPrice());
            assertEquals(status.tradePrices().getPrice(event.goodType()), event.unitPrice());
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
        IslandStatus status = IslandStatus.empty(island.id());
        Ship ship = Ship.create("Black Pearl", island.id(), null, null, Inventory.empty());

        TradeResult result = new ArrivalTrade(new Random(1)).execute(ship, island, status, 40);

        assertTrue(result.events().isEmpty());
        assertEquals(Inventory.empty(), result.ship().getInventory());
        assertEquals(Inventory.empty(), result.islandStatus().inventory());
    }

    @Test
    void executePaysBuyTaxToIsland() {
        Island island = Island.create("Jamaica", new Footprint(0, 0, 10, 10));
        IslandStatus status = new IslandStatus(
                island.id(),
                1_000,
                Inventory.of(Map.of(GoodType.RUM, 10)),
                TradePriceList.of(Map.of(
                        GoodType.FOOD, 100,
                        GoodType.RUM, 100,
                        GoodType.SUGAR, 100,
                        GoodType.SPICES, 100,
                        GoodType.TOBACCO, 100
                ))
        );
        Ship ship = Ship.create("Black Pearl", island.id(), null, null, Inventory.of(Map.of(GoodType.GOLD, 1_200)));

        TradeResult result = new ArrivalTrade(new Random(1)).execute(ship, island, status, 40);

        assertFalse(result.events().isEmpty());
        int expectedPaid = 0;
        int expectedTax = 0;
        for (TradeEvent event : result.events()) {
            assertEquals(TradeType.BUY_FROM_ISLAND, event.tradeType());
            int goodsCost = event.unitPrice() * event.amount();
            int tax = ArrivalTrade.buyTax(event.unitPrice()) * event.amount();
            assertEquals(goodsCost + tax, event.totalPrice());
            expectedPaid += event.totalPrice();
            expectedTax += tax;
        }
        assertTrue(expectedTax > 0);
        assertEquals(1_200 - expectedPaid, result.ship().getInventory().getAmount(GoodType.GOLD));
        assertEquals(expectedPaid, result.islandStatus().inventory().getAmount(GoodType.GOLD));
    }

    @Test
    void executeDoesNotTaxSales() {
        Island island = Island.create("Jamaica", new Footprint(0, 0, 10, 10));
        IslandStatus status = new IslandStatus(
                island.id(),
                1_000,
                Inventory.of(Map.of(GoodType.GOLD, 100)),
                TradePriceList.of(Map.of(
                        GoodType.FOOD, 10,
                        GoodType.RUM, 10,
                        GoodType.SUGAR, 10,
                        GoodType.SPICES, 10,
                        GoodType.TOBACCO, 10
                ))
        );
        Ship ship = Ship.create(
                "Black Pearl",
                island.id(),
                null,
                null,
                Inventory.of(Map.of(GoodType.RUM, 5, GoodType.GOLD, 20))
        );

        TradeResult result = new ArrivalTrade(new Random(1)).execute(ship, island, status, 40);

        assertFalse(result.events().isEmpty());
        int expectedGoodsCost = 0;
        for (TradeEvent event : result.events()) {
            assertEquals(TradeType.SELL_TO_ISLAND, event.tradeType());
            expectedGoodsCost += event.totalPrice();
        }
        assertEquals(20 + expectedGoodsCost, result.ship().getInventory().getAmount(GoodType.GOLD));
        assertEquals(100 - expectedGoodsCost, result.islandStatus().inventory().getAmount(GoodType.GOLD));
    }
}
