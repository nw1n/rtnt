package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record WeatherSample(long tick, int temperature) {
}
