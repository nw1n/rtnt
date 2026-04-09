package com.example.rtnt.domain.island;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.location.Footprint;

/**
 * Interface representing an island in the game.
 */
public interface Island {
    /**
     * Get the unique identifier of this island.
     * @return the island's ID
     */
    String getId();

    /**
     * Get the name of this island.
     * @return the island's name
     */
    String getName();

    /**
     * Get the footprint of this island.
     * @return the island's geographical footprint (x, y, width, length)
     */
    Footprint getFootprint();

    /**
     * Get the inventory currently available on this island.
     * @return inventory with tracked goods
     */
    Inventory getInventory();

    /**
     * Get gold-denominated trade prices for goods available on this island.
     * GOLD itself is excluded from this price list.
     *
     * @return trade prices for non-gold goods
     */
    TradePriceList getTradePrices();
}
