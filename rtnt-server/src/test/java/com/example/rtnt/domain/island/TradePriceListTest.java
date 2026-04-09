package com.example.rtnt.domain.island;

import com.example.rtnt.domain.inventory.GoodType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradePriceListTest {

    @Test
    void setPriceUpdatesValueForGood() {
        TradePriceList prices = TradePriceList.defaultPrices();

        prices.setPrice(GoodType.RUM, 7);

        assertEquals(7, prices.getPrice(GoodType.RUM));
    }

    @Test
    void ofRequiresAllTradeGoods() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TradePriceList.of(Map.of(
                        GoodType.RUM, 2,
                        GoodType.SUGAR, 3
                ))
        );

        assertEquals("Missing or invalid price for SPICES", exception.getMessage());
    }

    @Test
    void goldCannotBePriced() {
        TradePriceList prices = TradePriceList.defaultPrices();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> prices.setPrice(GoodType.GOLD, 10)
        );

        assertEquals("GOLD cannot have a trade price", exception.getMessage());
    }
}
