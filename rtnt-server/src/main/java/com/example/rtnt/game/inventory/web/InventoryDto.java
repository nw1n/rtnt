package com.example.rtnt.game.inventory.web;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;

public record InventoryDto(int gold, int rum, int sugar, int spices, int tobacco) {
    public static InventoryDto from(Inventory inventory) {
        return new InventoryDto(
                inventory.getAmount(GoodType.GOLD),
                inventory.getAmount(GoodType.RUM),
                inventory.getAmount(GoodType.SUGAR),
                inventory.getAmount(GoodType.SPICES),
                inventory.getAmount(GoodType.TOBACCO)
        );
    }
}
