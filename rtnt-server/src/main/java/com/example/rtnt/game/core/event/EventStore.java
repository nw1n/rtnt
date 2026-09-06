package com.example.rtnt.game.core.event;

import com.example.rtnt.game.weather.TemperatureChanged;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public interface EventStore {
    String WORLD_STREAM = "world";

    void append(TemperatureChanged event);

    List<TemperatureChanged> readAll();
}
