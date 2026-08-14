package com.example.rtnt.domain.island;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.location.Footprint;

import java.util.Objects;
import java.util.UUID;

public class Island {
    private final String id;
    private final String name;
    private final Footprint footprint;
    private final Inventory inventory;
    private final TradePriceList tradePrices;

    private Island(
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

    public static Island create(String name, Footprint footprint, Inventory inventory, TradePriceList tradePrices) {
        return new Island(UUID.randomUUID().toString(), name, footprint, inventory, tradePrices);
    }

    public static Island existing(
            String id,
            String name,
            Footprint footprint,
            Inventory inventory,
            TradePriceList tradePrices
    ) {
        return new Island(id, name, footprint, inventory, tradePrices);
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Footprint getFootprint() {
        return this.footprint;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public TradePriceList getTradePrices() {
        return this.tradePrices;
    }
}
