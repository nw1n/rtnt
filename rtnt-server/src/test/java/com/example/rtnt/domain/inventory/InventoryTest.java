package com.example.rtnt.domain.inventory;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryTest {

    @Test
    void emptyInitializesAllGoodsWithZero() {
        Inventory inventory = Inventory.empty();

        assertEquals(0, inventory.getAmount(GoodType.GOLD));
        assertEquals(0, inventory.getAmount(GoodType.RUM));
        assertEquals(0, inventory.getAmount(GoodType.SUGAR));
        assertEquals(0, inventory.getAmount(GoodType.SPICES));
        assertEquals(0, inventory.getAmount(GoodType.TOBACCO));
    }

    @Test
    void setAmountUpdatesSpecificGood() {
        Inventory inventory = Inventory.empty();

        inventory.setAmount(GoodType.RUM, 42);

        assertEquals(42, inventory.getAmount(GoodType.RUM));
    }

    @Test
    void addAmountIncreasesExistingValue() {
        Inventory inventory = Inventory.of(Map.of(GoodType.SPICES, 5));

        inventory.addAmount(GoodType.SPICES, 3);

        assertEquals(8, inventory.getAmount(GoodType.SPICES));
    }

    @Test
    void removeAmountDecreasesExistingValue() {
        Inventory inventory = Inventory.of(Map.of(GoodType.SUGAR, 9));

        inventory.removeAmount(GoodType.SUGAR, 4);

        assertEquals(5, inventory.getAmount(GoodType.SUGAR));
    }

    @Test
    void removeAmountThrowsWhenRemovingMoreThanAvailable() {
        Inventory inventory = Inventory.of(Map.of(GoodType.TOBACCO, 2));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventory.removeAmount(GoodType.TOBACCO, 3)
        );

        assertEquals("Cannot remove more than available inventory for TOBACCO", exception.getMessage());
    }

    @Test
    void setAmountThrowsOnNegativeValue() {
        Inventory inventory = Inventory.empty();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventory.setAmount(GoodType.GOLD, -1)
        );

        assertEquals("Inventory amount cannot be negative for GOLD", exception.getMessage());
    }

    @Test
    void addAmountThrowsOnNegativeIncrement() {
        Inventory inventory = Inventory.empty();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventory.addAmount(GoodType.RUM, -1)
        );

        assertEquals("Amount to add cannot be negative", exception.getMessage());
    }

    @Test
    void removeAmountThrowsOnNegativeDecrement() {
        Inventory inventory = Inventory.empty();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventory.removeAmount(GoodType.RUM, -1)
        );

        assertEquals("Amount to remove cannot be negative", exception.getMessage());
    }

    @Test
    void sumTradeableGoodsIgnoresGold() {
        Inventory inventory = Inventory.of(Map.of(
                GoodType.GOLD, 999,
                GoodType.RUM, 2,
                GoodType.SUGAR, 3,
                GoodType.SPICES, 4,
                GoodType.TOBACCO, 5
        ));

        assertEquals(14, inventory.sumTradeableGoods());
    }
}
