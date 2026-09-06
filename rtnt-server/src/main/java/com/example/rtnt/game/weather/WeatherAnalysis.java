package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public record WeatherAnalysis(
        long fromTick,
        long toTick,
        int startTemperature,
        int endTemperature,
        double averageTemperature,
        int minTemperature,
        int maxTemperature,
        int changeCount,
        int largeChangeCount,
        int reversals,
        int burstCount,
        int largeDeltaThreshold,
        int burstWindowTicks
) {
    public static final int LARGE_DELTA_THRESHOLD = 3;
    public static final int BURST_WINDOW_TICKS = 50;

    public static WeatherAnalysis of(
            List<TemperatureChanged> events,
            long currentTick,
            @Nullable Long fromTick,
            @Nullable Long toTick
    ) {
        List<WeatherSample> samples = Weather.history(events, currentTick, fromTick, toTick);
        long start = samples.getFirst().tick();
        long end = samples.getLast().tick();
        int startTemperature = samples.getFirst().temperature();
        int endTemperature = samples.getLast().temperature();
        int minTemperature = startTemperature;
        int maxTemperature = startTemperature;
        double weighted = 0;
        for (int i = 0; i < samples.size(); i++) {
            WeatherSample sample = samples.get(i);
            minTemperature = Math.min(minTemperature, sample.temperature());
            maxTemperature = Math.max(maxTemperature, sample.temperature());
            if (i + 1 < samples.size()) {
                weighted += (samples.get(i + 1).tick() - sample.tick()) * (double) sample.temperature();
            }
        }
        long duration = end - start;
        double average = duration == 0 ? startTemperature : weighted / duration;
        List<TemperatureChanged> inRange = events.stream()
                .filter(event -> event.tick() >= start && event.tick() <= end)
                .toList();
        int largeChangeCount = 0;
        int reversals = 0;
        int previousSign = 0;
        for (TemperatureChanged event : inRange) {
            if (Math.abs(event.delta()) >= LARGE_DELTA_THRESHOLD) {
                largeChangeCount++;
            }
            int sign = Integer.signum(event.delta());
            if (previousSign != 0 && sign != previousSign) {
                reversals++;
            }
            previousSign = sign;
        }
        return new WeatherAnalysis(
                start,
                end,
                startTemperature,
                endTemperature,
                average,
                minTemperature,
                maxTemperature,
                inRange.size(),
                largeChangeCount,
                reversals,
                burstCount(inRange),
                LARGE_DELTA_THRESHOLD,
                BURST_WINDOW_TICKS
        );
    }

    private static int burstCount(List<TemperatureChanged> events) {
        int bursts = 0;
        int index = 0;
        while (index < events.size()) {
            int next = index + 1;
            while (next < events.size()
                    && events.get(next).tick() - events.get(index).tick() <= BURST_WINDOW_TICKS) {
                next++;
            }
            if (next - index >= 2) {
                bursts++;
                index = next;
            } else {
                index++;
            }
        }
        return bursts;
    }
}
