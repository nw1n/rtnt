package com.example.rtnt.domain.ship;

import com.example.rtnt.domain.inventory.Inventory;

/**
 * Interface representing a ship in the game.
 */
public interface Ship {
    /**
     * Get the unique identifier of this ship.
     * @return the ship's ID
     */
    String getId();

    /**
     * Get the name of this ship.
     * @return the ship's name
     */
    String getName();

    /**
     * Get the island where this ship is currently anchored.
     *
     * @return current island ID or null while in transit
     */
    String getIslandId();

    /**
     * Get the player currently controlling this ship.
     * Can be null if no player is assigned yet.
     *
     * @return player ID or null
     */
    String getPlayerId();

    Journey getJourney();

    /**
     * Get ship speed.
     *
     * @return static speed value
     */
    int getSpeed();

    /**
     * Max units of tradeable goods in the hold. Gold does not count toward this limit.
     */
    int getCargoCapacity();

    /**
     * Get the inventory currently held by this ship.
     * @return inventory with tracked goods
     */
    Inventory getInventory();
}
