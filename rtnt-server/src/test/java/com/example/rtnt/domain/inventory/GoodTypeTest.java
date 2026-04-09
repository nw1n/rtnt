package com.example.rtnt.domain.inventory;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoodTypeTest {

    @Test
    void tradeableFlagsAreDefinedCorrectly() {
        assertFalse(GoodType.GOLD.isTradeable());
        assertTrue(GoodType.RUM.isTradeable());
        assertTrue(GoodType.SUGAR.isTradeable());
        assertTrue(GoodType.SPICES.isTradeable());
        assertTrue(GoodType.TOBACCO.isTradeable());
    }

    @Test
    void tradeableGoodsContainsOnlyTradeableTypes() {
        Set<GoodType> tradeable = GoodType.tradeableGoods();

        assertEquals(Set.of(GoodType.RUM, GoodType.SUGAR, GoodType.SPICES, GoodType.TOBACCO), tradeable);
    }
}
