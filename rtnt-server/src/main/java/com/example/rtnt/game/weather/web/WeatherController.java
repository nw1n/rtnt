package com.example.rtnt.game.weather.web;

import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.weather.Weather;
import com.example.rtnt.game.weather.WeatherAnalysis;
import com.example.rtnt.game.weather.WeatherSample;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public List<WeatherSampleDto> history(
            @RequestParam(required = false) Long fromTick,
            @RequestParam(required = false) Long toTick
    ) {
        return this.gameLoop.weatherHistory(fromTick, toTick).stream()
                .map(WeatherSampleDto::from)
                .toList();
    }

    @GetMapping("/analysis")
    public WeatherAnalysisDto analysis(
            @RequestParam(required = false) Long fromTick,
            @RequestParam(required = false) Long toTick
    ) {
        return WeatherAnalysisDto.from(this.gameLoop.weatherAnalysis(fromTick, toTick));
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

    public record WeatherAnalysisDto(
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
        static WeatherAnalysisDto from(WeatherAnalysis analysis) {
            return new WeatherAnalysisDto(
                    analysis.fromTick(),
                    analysis.toTick(),
                    analysis.startTemperature(),
                    analysis.endTemperature(),
                    analysis.averageTemperature(),
                    analysis.minTemperature(),
                    analysis.maxTemperature(),
                    analysis.changeCount(),
                    analysis.largeChangeCount(),
                    analysis.reversals(),
                    analysis.burstCount(),
                    analysis.largeDeltaThreshold(),
                    analysis.burstWindowTicks()
            );
        }
    }
}
