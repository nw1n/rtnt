package com.example.rtnt.game.weather.web;

import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.weather.Weather;
import com.example.rtnt.game.weather.WeatherSample;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final GameLoop gameLoop;

    public WeatherController(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    @GetMapping
    public WeatherDto get() {
        return WeatherDto.from(this.gameLoop.weather());
    }

    @GetMapping("/history")
    public List<WeatherSampleDto> history() {
        return this.gameLoop.weatherHistory().stream()
                .map(WeatherSampleDto::from)
                .toList();
    }

    public record WeatherDto(int temperature) {
        static WeatherDto from(Weather weather) {
            return new WeatherDto(weather.temperature());
        }
    }

    public record WeatherSampleDto(long tick, int temperature) {
        static WeatherSampleDto from(WeatherSample sample) {
            return new WeatherSampleDto(sample.tick(), sample.temperature());
        }
    }
}
