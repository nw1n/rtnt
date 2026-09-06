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

    @Test
    void historyStartsAtInitialTemperatureAndHoldsThroughQuietTicks() {
        List<WeatherSample> samples = Weather.history(
                List.of(new TemperatureChanged(10, 3), new TemperatureChanged(20, -2)),
                25
        );

        assertEquals(List.of(
                new WeatherSample(0, 20),
                new WeatherSample(10, 23),
                new WeatherSample(20, 21),
                new WeatherSample(25, 21)
        ), samples);
    }

    @Test
    void historyClipsToSelectedTicks() {
        List<WeatherSample> samples = Weather.history(
                List.of(new TemperatureChanged(10, 3), new TemperatureChanged(20, -2)),
                25,
                12L,
                22L
        );

        assertEquals(List.of(
                new WeatherSample(12, 23),
                new WeatherSample(20, 21),
                new WeatherSample(22, 21)
        ), samples);
    }
}
