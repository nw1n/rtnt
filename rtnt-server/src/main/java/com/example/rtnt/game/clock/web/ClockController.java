package com.example.rtnt.game.clock.web;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.service.ClockService;
import com.example.rtnt.game.loop.GameLoop;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clock")
public class ClockController {
    private final ClockService clockService;
    private final GameLoop gameLoop;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ClockController(ClockService clockService, GameLoop gameLoop) {
        this.clockService = clockService;
        this.gameLoop = gameLoop;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public ClockDto get() {
        return ClockDto.from(this.clockService.get());
    }

    @PostMapping("/pause")
    public ClockDto pause() {
        return ClockDto.from(this.clockService.pause());
    }

    @PostMapping("/resume")
    public ClockDto resume() {
        return ClockDto.from(this.clockService.resume());
    }

    @PostMapping("/mode")
    public ClockDto setMode(@Valid @RequestBody ModeRequest request) {
        return ClockDto.from(this.clockService.setMode(request.mode()));
    }

    @PostMapping("/advance")
    public ClockDto advance(@Valid @RequestBody AdvanceRequest request) {
        return ClockDto.from(this.gameLoop.advance(request.ticks()));
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record ClockDto(long tick, ClockMode mode, boolean paused) {
        static ClockDto from(GameClock clock) {
            return new ClockDto(clock.tick(), clock.mode(), clock.paused());
        }
    }

    public record ModeRequest(@NotNull ClockMode mode) {
    }

    public record AdvanceRequest(@Min(1) @Max(100_000) int ticks) {
    }
}
