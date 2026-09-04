package com.example.rtnt.game.inventory.persistence;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@NullMarked
public record InventoryDocument(@Nullable Map<GoodType, Integer> amounts) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static InventoryDocument from(Inventory inventory) {
        return new InventoryDocument(new EnumMap<>(inventory.amounts()));
    }

    public static Inventory toInventory(@Nullable InventoryDocument document) {
        if (document == null || document.amounts() == null) {
            return Inventory.empty();
        }
        return Inventory.of(document.amounts());
    }
}
