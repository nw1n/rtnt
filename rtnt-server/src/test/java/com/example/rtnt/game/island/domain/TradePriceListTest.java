package com.example.rtnt.game.island.domain;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradePriceListTest {

    @Test
    void ofRequiresPositivePricesForTradeableGoods() {
        TradePriceList prices = TradePriceList.of(Map.of(
                GoodType.FOOD, 2,
                GoodType.RUM, 3,
                GoodType.SUGAR, 2,
                GoodType.SPICES, 4,
                GoodType.TOBACCO, 5
        ));
        assertEquals(2, prices.getPrice(GoodType.FOOD));
        assertEquals(3, prices.getPrice(GoodType.RUM));
        assertEquals(2, prices.getPrice(GoodType.SUGAR));
        assertEquals(4, prices.getPrice(GoodType.SPICES));
        assertEquals(5, prices.getPrice(GoodType.TOBACCO));
    }

    @Test
    void goldHasNoTradePrice() {
        TradePriceList prices = TradePriceList.defaultPrices();
        assertThrows(IllegalArgumentException.class, () -> prices.getPrice(GoodType.GOLD));
    }

    @Test
    void withPriceReturnsNewList() {
        TradePriceList start = TradePriceList.islandSeed();
        TradePriceList next = start.withPrice(GoodType.RUM, 9);
        assertEquals(3, start.getPrice(GoodType.RUM));
        assertEquals(9, next.getPrice(GoodType.RUM));
        assertEquals(2, next.getPrice(GoodType.SUGAR));
    }

    @Test
    void adjustForSupplyRaisesScarceAndLowersSurplus() {
        TradePriceList start = TradePriceList.of(Map.of(GoodType.RUM, 4, GoodType.SUGAR, 4, GoodType.FOOD, 4));
        Inventory stock = Inventory.of(Map.of(
                GoodType.RUM, 2,
                GoodType.SUGAR, 35,
                GoodType.FOOD, 15,
                GoodType.SPICES, 15,
                GoodType.TOBACCO, 15
        ));
        TradePriceList next = start.adjustForSupply(stock, 10, 30, 1, 20);
        assertEquals(5, next.getPrice(GoodType.RUM));
        assertEquals(3, next.getPrice(GoodType.SUGAR));
        assertEquals(4, next.getPrice(GoodType.FOOD));
    }

    @Test
    void rejectsNonPositivePrice() {
        TradePriceList prices = TradePriceList.defaultPrices();
        assertThrows(IllegalArgumentException.class, () -> prices.withPrice(GoodType.RUM, 0));
    }
}
