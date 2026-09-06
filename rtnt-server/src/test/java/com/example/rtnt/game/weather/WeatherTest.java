package com.example.rtnt.game.weather;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherTest {

    @Test
    void initialTemperatureIsTwenty() {
        assertEquals(20, Weather.initial().temperature());
    }

    @Test
    void applyFoldsRecordedDeltas() {
        Weather weather = Weather.initial().applyAll(List.of(
                new TemperatureChanged(10, 3),
                new TemperatureChanged(20, -2)
        ));

        assertEquals(21, weather.temperature());
    }
}
