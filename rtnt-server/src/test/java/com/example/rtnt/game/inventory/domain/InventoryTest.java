package com.example.rtnt.game.inventory.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    @Test
    void emptyHasZeroOfEveryGood() {
        Inventory inventory = Inventory.empty();
        for (GoodType goodType : GoodType.values()) {
            assertEquals(0, inventory.getAmount(goodType));
        }
        assertEquals(0, inventory.sumTradeableGoods());
    }

    @Test
    void ofFillsMissingGoodsWithZero() {
        Inventory inventory = Inventory.of(Map.of(GoodType.GOLD, 50, GoodType.RUM, 3));
        assertEquals(50, inventory.getAmount(GoodType.GOLD));
        assertEquals(3, inventory.getAmount(GoodType.RUM));
        assertEquals(0, inventory.getAmount(GoodType.SUGAR));
        assertEquals(3, inventory.sumTradeableGoods());
    }

    @Test
    void ofRejectsNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Inventory.of(Map.of(GoodType.GOLD, -1)));
    }

    @Test
    void addAndRemoveReturnNewInventory() {
        Inventory start = Inventory.of(Map.of(GoodType.RUM, 4));
        Inventory added = start.addAmount(GoodType.RUM, 2);
        Inventory removed = added.removeAmount(GoodType.RUM, 1);
        assertEquals(4, start.getAmount(GoodType.RUM));
        assertEquals(6, added.getAmount(GoodType.RUM));
        assertEquals(5, removed.getAmount(GoodType.RUM));
    }

    @Test
    void removeRejectsMoreThanAvailable() {
        Inventory inventory = Inventory.of(Map.of(GoodType.SUGAR, 1));
        assertThrows(IllegalArgumentException.class, () -> inventory.removeAmount(GoodType.SUGAR, 2));
    }

    @Test
    void goldDoesNotCountAsTradeableHold() {
        Inventory inventory = Inventory.of(Map.of(GoodType.GOLD, 1000, GoodType.TOBACCO, 6));
        assertEquals(6, inventory.sumTradeableGoods());
    }

    @Test
    void allTradeableAtLeastRequiresEveryTradeableGood() {
        Inventory inventory = Inventory.of(Map.of(
                GoodType.GOLD, 1,
                GoodType.FOOD, 19,
                GoodType.RUM, 20,
                GoodType.SUGAR, 20,
                GoodType.SPICES, 20,
                GoodType.TOBACCO, 19
        ));
        assertFalse(inventory.allTradeableAtLeast(20));
        assertTrue(inventory.allTradeableAtLeast(19));
    }

    @Test
    void consumeHalfTradeableGoodsLeavesGold() {
        Inventory halved = Inventory.of(Map.of(
                GoodType.GOLD, 80,
                GoodType.RUM, 21,
                GoodType.SUGAR, 10
        )).consumeHalfTradeableGoods();
        assertEquals(80, halved.getAmount(GoodType.GOLD));
        assertEquals(10, halved.getAmount(GoodType.RUM));
        assertEquals(5, halved.getAmount(GoodType.SUGAR));
    }
}
