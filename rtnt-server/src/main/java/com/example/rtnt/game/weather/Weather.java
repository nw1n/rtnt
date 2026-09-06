package com.example.rtnt.game.weather;

import org.jspecify.annotations.NullMarked;

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
}
