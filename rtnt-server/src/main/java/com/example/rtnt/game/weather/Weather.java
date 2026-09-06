package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public record Weather(int temperature) {
    public static Weather initial() {
        return new Weather(20);
    }

    public Weather apply(TemperatureChanged event) {
        return new Weather(this.temperature + event.delta());
    }

    public Weather applyAll(Iterable<TemperatureChanged> events) {
        Weather weather = this;
        for (TemperatureChanged event : events) {
            weather = weather.apply(event);
        }
        return weather;
    }

    public static List<WeatherSample> history(List<TemperatureChanged> events, long currentTick) {
        if (currentTick < 0) {
            throw new IllegalArgumentException("currentTick must be >= 0");
        }
        List<WeatherSample> samples = new ArrayList<>();
        Weather weather = initial();
        samples.add(new WeatherSample(0, weather.temperature()));
        for (TemperatureChanged event : events) {
            weather = weather.apply(event);
            if (event.tick() == 0) {
                samples.set(0, new WeatherSample(0, weather.temperature()));
            } else {
                samples.add(new WeatherSample(event.tick(), weather.temperature()));
            }
        }
        WeatherSample last = samples.getLast();
        if (currentTick > last.tick()) {
            samples.add(new WeatherSample(currentTick, last.temperature()));
        }
        return List.copyOf(samples);
    }
}
