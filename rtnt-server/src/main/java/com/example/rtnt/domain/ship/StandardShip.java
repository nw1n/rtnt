package com.example.rtnt.domain.ship;

import com.example.rtnt.domain.inventory.Inventory;

import java.util.Objects;
import java.util.UUID;

public class StandardShip implements Ship {
    private static final int SPEED = 20;
    /** Max units of tradeable goods in the hold. Gold does not count toward this limit. */
    private static final int CARGO_CAPACITY_UNITS = 100;

    private final String id;
    private final String name;
    private final String islandId;
    private final Journey journey;
    private final String playerId;
    private final Inventory inventory;

    private StandardShip(String id, String name, String islandId, Journey journey, String playerId, Inventory inventory) {
        this.id = Objects.requireNonNull(id, "Ship id cannot be null");
        this.name = Objects.requireNonNull(name, "Ship name cannot be null");
        this.islandId = islandId;
        this.journey = journey;
        this.playerId = playerId;
        this.inventory = Objects.requireNonNull(inventory, "Ship inventory cannot be null");
    }

    public static StandardShip create(String name, String islandId, Journey journey, String playerId, Inventory inventory) {
        return new StandardShip(UUID.randomUUID().toString(), name, islandId, journey, playerId, inventory);
    }

    public static StandardShip fromDocument(String id, String name, String islandId, Journey journey, String playerId, Inventory inventory) {
        return new StandardShip(id, name, islandId, journey, playerId, inventory);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getIslandId() {
        return this.islandId;
    }

    @Override
    public Journey getJourney() {
        return this.journey;
    }

    @Override
    public String getPlayerId() {
        return this.playerId;
    }

    @Override
    public int getSpeed() {
        return SPEED;
    }

    @Override
    public int getCargoCapacity() {
        return CARGO_CAPACITY_UNITS;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        StandardShip ship = (StandardShip) o;
        return Objects.equals(this.id, ship.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "StandardShip{id='" + this.id + "', name='" + this.name + "', islandId='" + this.islandId + "', journey=" + this.journey + ", playerId='" + this.playerId + "', inventory=" + this.inventory + "}";
    }
}
