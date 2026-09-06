package com.example.rtnt.game.weather;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherChangeTest {

    @Test
    void skipsTicksOffInterval() {
        WeatherChange change = new WeatherChange(10, 1.0, -2, 2, new Random(1));

        assertTrue(change.decide(0).isEmpty());
        assertTrue(change.decide(9).isEmpty());
    }

    @Test
    void drawsWhenChanceIsCertain() {
        WeatherChange change = new WeatherChange(10, 1.0, 2, 2, new Random(1));

        TemperatureChanged event = change.decide(10).orElseThrow();

        assertEquals(10, event.tick());
        assertEquals(2, event.delta());
    }

    @Test
    void doesNotDrawWhenChanceIsZero() {
        WeatherChange change = new WeatherChange(10, 0.0, -2, 2, new Random(1));

        assertTrue(change.decide(10).isEmpty());
    }
}
