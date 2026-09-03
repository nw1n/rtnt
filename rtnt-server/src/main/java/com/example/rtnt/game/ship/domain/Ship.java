package com.example.rtnt.game.ship.domain;

import com.example.rtnt.game.inventory.domain.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@NullMarked
public class Ship {
    private static final int SPEED = 20;
    /** Max units of tradeable goods in the hold. Gold does not count toward this limit. */
    private static final int CARGO_CAPACITY_UNITS = 100;

    private final String id;
    private final String name;
    private final @Nullable String islandId;
    private final @Nullable Journey journey;
    private final @Nullable String playerId;
    private final Inventory inventory;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    private Ship(
            String id,
            String name,
            @Nullable String islandId,
            @Nullable Journey journey,
            @Nullable String playerId,
            Inventory inventory
    ) {
        this.id = Objects.requireNonNull(id, "Ship id cannot be null");
        this.name = Objects.requireNonNull(name, "Ship name cannot be null");
        this.islandId = islandId;
        this.journey = journey;
        this.playerId = playerId;
        this.inventory = Objects.requireNonNull(inventory, "Ship inventory cannot be null");
    }

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static Ship create(
            String name,
            @Nullable String islandId,
            @Nullable Journey journey,
            @Nullable String playerId,
            Inventory inventory
    ) {
        return new Ship(UUID.randomUUID().toString(), name, islandId, journey, playerId, inventory);
    }

    public static Ship existing(
            String id,
            String name,
            @Nullable String islandId,
            @Nullable Journey journey,
            @Nullable String playerId,
            Inventory inventory
    ) {
        return new Ship(id, name, islandId, journey, playerId, inventory);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public @Nullable String getIslandId() {
        return this.islandId;
    }

    public @Nullable Journey getJourney() {
        return this.journey;
    }

    public @Nullable String getPlayerId() {
        return this.playerId;
    }

    public int getSpeed() {
        return SPEED;
    }

    public int getCargoCapacity() {
        return CARGO_CAPACITY_UNITS;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ship ship)) {
            return false;
        }
        return Objects.equals(this.id, ship.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "Ship{id='" + this.id + "', name='" + this.name + "', islandId='" + this.islandId
                + "', journey=" + this.journey + ", playerId='" + this.playerId + "', inventory=" + this.inventory + "}";
    }
}
