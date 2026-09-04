package com.example.rtnt.game.island.domain;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IslandStatusTest {

    @Test
    void initialPopulationIsZero() {
        IslandStatus status = IslandStatus.initial("island-1");
        assertEquals("island-1", status.islandId());
        assertEquals(0, status.population());
        assertEquals(Inventory.islandSeed(), status.inventory());
        assertEquals(TradePriceList.islandSeed(), status.tradePrices());
    }

    @Test
    void growIncreasesPopulation() {
        IslandStatus grown = IslandStatus.initial("island-1").grow(4);
        assertEquals(4, grown.population());
        assertEquals("island-1", grown.islandId());
        assertEquals(Inventory.islandSeed(), grown.inventory());
        assertEquals(TradePriceList.islandSeed(), grown.tradePrices());
    }

    @Test
    void produceAddsTradeableGood() {
        IslandStatus produced = IslandStatus.initial("island-1").produce(GoodType.RUM, 5);
        assertEquals(15, produced.inventory().getAmount(GoodType.RUM));
        assertEquals(0, produced.population());
    }

    @Test
    void consumeHalfGoodsAndGrowHalvesTradeables() {
        IslandStatus flourished = IslandStatus.initial("island-1").consumeHalfGoodsAndGrow(2);
        assertEquals(2, flourished.population());
        assertEquals(5, flourished.inventory().getAmount(GoodType.RUM));
        assertEquals(5, flourished.inventory().getAmount(GoodType.SUGAR));
        assertEquals(100, flourished.inventory().getAmount(GoodType.GOLD));
    }
}
