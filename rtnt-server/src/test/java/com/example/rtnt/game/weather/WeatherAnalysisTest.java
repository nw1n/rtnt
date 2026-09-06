package com.example.rtnt.game.weather;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherAnalysisTest {

    @Test
    void timeWeightedAverageAndVolatility() {
        WeatherAnalysis analysis = WeatherAnalysis.of(
                List.of(
                        new TemperatureChanged(10, 3),
                        new TemperatureChanged(20, -3),
                        new TemperatureChanged(30, 2)
                ),
                40,
                null,
                null
        );

        assertEquals(0, analysis.fromTick());
        assertEquals(40, analysis.toTick());
        assertEquals(20, analysis.startTemperature());
        assertEquals(22, analysis.endTemperature());
        assertEquals(21.25, analysis.averageTemperature());
        assertEquals(20, analysis.minTemperature());
        assertEquals(23, analysis.maxTemperature());
        assertEquals(3, analysis.changeCount());
        assertEquals(2, analysis.largeChangeCount());
        assertEquals(2, analysis.reversals());
        assertEquals(1, analysis.burstCount());
    }
}
