package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record TemperatureChanged(long tick, int delta) {
    public static final String TYPE = "TemperatureChanged";

    public TemperatureChanged {
        if (tick < 0) {
            throw new IllegalArgumentException("tick must be >= 0");
        }
        if (delta == 0) {
            throw new IllegalArgumentException("delta must not be 0");
        }
    }
}
