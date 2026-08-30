package com.example.rtnt.game.core.loop.web;

import com.example.rtnt.game.core.loop.ClockMode;
import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.core.loop.GameLoopStatus;
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
@RequestMapping("/api/game-loop")
public class GameLoopController {
    private final GameLoop gameLoop;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLoopController(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public GameLoopDto get() {
        return GameLoopDto.from(this.gameLoop.get());
    }

    @PostMapping("/pause")
    public GameLoopDto pause() {
        return GameLoopDto.from(this.gameLoop.pause());
    }

    @PostMapping("/resume")
    public GameLoopDto resume() {
        return GameLoopDto.from(this.gameLoop.resume());
    }

    @PostMapping("/mode")
    public GameLoopDto setMode(@Valid @RequestBody ModeRequest request) {
        return GameLoopDto.from(this.gameLoop.setMode(request.mode()));
    }

    @PostMapping("/advance")
    public GameLoopDto advance(@Valid @RequestBody AdvanceRequest request) {
        return GameLoopDto.from(this.gameLoop.advance(request.ticks()));
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record GameLoopDto(long tick, ClockMode mode, boolean paused) {
        static GameLoopDto from(GameLoopStatus status) {
            return new GameLoopDto(status.tick(), status.mode(), status.paused());
        }
    }

    public record ModeRequest(@NotNull ClockMode mode) {
    }

    public record AdvanceRequest(@Min(1) @Max(100_000) int ticks) {
    }
}
