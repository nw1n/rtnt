package com.example.rtnt.game.island.domain;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IslandStatusTest {

    @Test
    void initialPopulationIsOneThousand() {
        IslandStatus status = IslandStatus.initial("island-1");
        assertEquals("island-1", status.islandId());
        assertEquals(1_000, status.population());
        assertEquals(Inventory.islandSeed(), status.inventory());
        assertEquals(TradePriceList.islandSeed(), status.tradePrices());
    }

    @Test
    void growIncreasesPopulation() {
        IslandStatus grown = IslandStatus.initial("island-1").grow(4);
        assertEquals(1_004, grown.population());
        assertEquals("island-1", grown.islandId());
        assertEquals(Inventory.islandSeed(), grown.inventory());
        assertEquals(TradePriceList.islandSeed(), grown.tradePrices());
    }

    @Test
    void produceAddsTradeableGood() {
        IslandStatus produced = IslandStatus.initial("island-1").produce(GoodType.RUM, 5);
        assertEquals(20, produced.inventory().getAmount(GoodType.RUM));
        assertEquals(1_000, produced.population());
    }

    @Test
    void consumeHalfGoodsAndGrowHalvesTradeables() {
        IslandStatus flourished = IslandStatus.initial("island-1").consumeHalfGoodsAndGrow(2);
        assertEquals(1_002, flourished.population());
        assertEquals(7, flourished.inventory().getAmount(GoodType.RUM));
        assertEquals(7, flourished.inventory().getAmount(GoodType.SUGAR));
        assertEquals(100, flourished.inventory().getAmount(GoodType.GOLD));
    }

    @Test
    void consumeFoodOrStarveEatsOneFoodPerTwoHundredFiftyPeople() {
        IslandStatus fed = IslandStatus.initial("island-1").consumeFoodOrStarve();
        assertEquals(1_000, fed.population());
        assertEquals(11, fed.inventory().getAmount(GoodType.FOOD));
    }

    @Test
    void consumeFoodOrStarveCutsPopulationWhenRationsFallShort() {
        IslandStatus hungry = new IslandStatus(
                "island-1",
                1_000,
                Inventory.of(Map.of(GoodType.FOOD, 3)),
                TradePriceList.defaultPrices()
        ).consumeFoodOrStarve();
        assertEquals(900, hungry.population());
        assertEquals(0, hungry.inventory().getAmount(GoodType.FOOD));
    }

    @Test
    void consumeFoodOrStarveCutsPopulationWhenHungry() {
        IslandStatus hungry = new IslandStatus(
                "island-1",
                11,
                Inventory.empty(),
                TradePriceList.defaultPrices()
        ).consumeFoodOrStarve();
        assertEquals(9, hungry.population());
    }

    @Test
    void spoilOverstockedGoodsHalvesOnlyThatGood() {
        IslandStatus spoiled = new IslandStatus(
                "island-1",
                8,
                Inventory.of(Map.of(GoodType.RUM, 41, GoodType.SUGAR, 10)),
                TradePriceList.defaultPrices()
        ).spoilOverstockedGoods(40);
        assertEquals(8, spoiled.population());
        assertEquals(20, spoiled.inventory().getAmount(GoodType.RUM));
        assertEquals(10, spoiled.inventory().getAmount(GoodType.SUGAR));
    }
}
