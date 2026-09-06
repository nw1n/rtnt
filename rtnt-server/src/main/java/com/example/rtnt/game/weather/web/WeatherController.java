package com.example.rtnt.game.weather.web;

import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.weather.Weather;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public record WeatherDto(int temperature) {
        static WeatherDto from(Weather weather) {
            return new WeatherDto(weather.temperature());
        }
    }
}
