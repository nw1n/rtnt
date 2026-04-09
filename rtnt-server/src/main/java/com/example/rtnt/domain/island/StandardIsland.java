package com.example.rtnt.domain.island;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.location.Footprint;

import java.util.Objects;
import java.util.UUID;

public class StandardIsland implements Island {
    private final String id;
    private final String name;
    private final Footprint footprint;
    private final Inventory inventory;
    private final TradePriceList tradePrices;

    private StandardIsland(
            String id,
            String name,
            Footprint footprint,
            Inventory inventory,
            TradePriceList tradePrices
    ) {
        this.id = Objects.requireNonNull(id, "Island id cannot be null");
        this.name = Objects.requireNonNull(name, "Island name cannot be null");
        this.footprint = Objects.requireNonNull(footprint, "Footprint cannot be null");
        this.inventory = Objects.requireNonNull(inventory, "Inventory cannot be null");
        this.tradePrices = Objects.requireNonNull(tradePrices, "Trade prices cannot be null");
    }

    public static StandardIsland create(
            String name,
            Footprint footprint,
            Inventory inventory,
            TradePriceList tradePrices) {
        return new StandardIsland(UUID.randomUUID().toString(), name, footprint, inventory, tradePrices);
    }

    public static StandardIsland fromDocument(
            String id,
            String name,
            Footprint footprint,
            Inventory inventory,
            TradePriceList tradePrices) {
        return new StandardIsland(id, name, footprint, inventory, tradePrices);
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
    public Footprint getFootprint() {
        return this.footprint;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public TradePriceList getTradePrices() {
        return this.tradePrices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        StandardIsland island = (StandardIsland) o;
        return Objects.equals(this.id, island.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "StandardIsland{id='" + this.id + "', name='" + this.name + "', footprint=" + this.footprint + ", inventory=" + this.inventory + ", tradePrices=" + this.tradePrices + "}";
    }
}
