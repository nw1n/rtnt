package com.example.rtnt.domain.island;

import java.util.Objects;
import java.util.UUID;

public class Island {
    private final String id;
    private final String name;
    private final Footprint footprint;

    private Island(String id, String name, Footprint footprint) {
        this.id = Objects.requireNonNull(id, "Island id cannot be null");
        this.name = Objects.requireNonNull(name, "Island name cannot be null");
        this.footprint = Objects.requireNonNull(footprint, "Footprint cannot be null");
    }

    public static Island create(String name, Footprint footprint) {
        return new Island(UUID.randomUUID().toString(), name, footprint);
    }

    public static Island existing(String id, String name, Footprint footprint) {
        return new Island(id, name, footprint);
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
}
