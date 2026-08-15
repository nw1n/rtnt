package com.example.rtnt.domain.island;

import java.util.Objects;
import java.util.UUID;

public record Island(String id, String name, Footprint footprint) {
    public Island {
        Objects.requireNonNull(id, "Island id cannot be null");
        Objects.requireNonNull(name, "Island name cannot be null");
        Objects.requireNonNull(footprint, "Footprint cannot be null");
    }

    public static Island create(String name, Footprint footprint) {
        return new Island(UUID.randomUUID().toString(), name, footprint);
    }

    public static Island existing(String id, String name, Footprint footprint) {
        return new Island(id, name, footprint);
    }
}
