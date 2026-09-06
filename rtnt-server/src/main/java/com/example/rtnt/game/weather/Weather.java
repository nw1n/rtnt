package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
        return history(events, currentTick, null, null);
    }

    public static List<WeatherSample> history(
            List<TemperatureChanged> events,
            long currentTick,
            @Nullable Long fromTick,
            @Nullable Long toTick
    ) {
        if (currentTick < 0) {
            throw new IllegalArgumentException("currentTick must be >= 0");
        }
        List<WeatherSample> full = fullHistory(events, currentTick);
        long end = toTick == null ? currentTick : clamp(toTick, 0, currentTick);
        long start = fromTick == null ? 0 : clamp(fromTick, 0, end);
        int temperature = full.getFirst().temperature();
        List<WeatherSample> samples = new ArrayList<>();
        for (WeatherSample sample : full) {
            if (sample.tick() <= start) {
                temperature = sample.temperature();
            }
        }
        samples.add(new WeatherSample(start, temperature));
        for (WeatherSample sample : full) {
            if (sample.tick() > start && sample.tick() <= end) {
                samples.add(sample);
                temperature = sample.temperature();
            }
        }
        if (samples.getLast().tick() < end) {
            samples.add(new WeatherSample(end, temperature));
        }
        return List.copyOf(samples);
    }

    private static List<WeatherSample> fullHistory(List<TemperatureChanged> events, long currentTick) {
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
        return samples;
    }

    private static long clamp(long value, long min, long max) {
        return Math.min(max, Math.max(min, value));
    }
}
