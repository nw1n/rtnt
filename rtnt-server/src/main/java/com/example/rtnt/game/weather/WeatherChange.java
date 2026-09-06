package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

@Component
@NullMarked
public class WeatherChange {
    private final int checkIntervalTicks;
    private final double changeChance;
    private final int changeMin;
    private final int changeMax;
    private final Random random;

    @Autowired
    public WeatherChange(
            @Value("${rtnt.weather.check-interval-ticks:10}") int checkIntervalTicks,
            @Value("${rtnt.weather.change-chance:0.5}") double changeChance,
            @Value("${rtnt.weather.change-min:-3}") int changeMin,
            @Value("${rtnt.weather.change-max:3}") int changeMax
    ) {
        this(checkIntervalTicks, changeChance, changeMin, changeMax, new Random());
    }

    WeatherChange(int checkIntervalTicks, double changeChance, int changeMin, int changeMax, Random random) {
        if (checkIntervalTicks < 1) {
            throw new IllegalArgumentException("checkIntervalTicks must be at least 1");
        }
        if (changeChance < 0 || changeChance > 1) {
            throw new IllegalArgumentException("changeChance must be between 0 and 1");
        }
        if (changeMin > changeMax) {
            throw new IllegalArgumentException("changeMin must be <= changeMax");
        }
        if (changeMin == 0 && changeMax == 0) {
            throw new IllegalArgumentException("change range must allow a non-zero delta");
        }
        this.checkIntervalTicks = checkIntervalTicks;
        this.changeChance = changeChance;
        this.changeMin = changeMin;
        this.changeMax = changeMax;
        this.random = random;
    }

    public Optional<TemperatureChanged> decide(long tick) {
        if (tick == 0 || tick % this.checkIntervalTicks != 0) {
            return Optional.empty();
        }
        if (this.random.nextDouble() >= this.changeChance) {
            return Optional.empty();
        }
        int delta = this.rollNonZeroDelta();
        return Optional.of(new TemperatureChanged(tick, delta));
    }

    private int rollNonZeroDelta() {
        int span = this.changeMax - this.changeMin + 1;
        int delta;
        do {
            delta = this.changeMin + this.random.nextInt(span);
        } while (delta == 0);
        return delta;
    }
}
