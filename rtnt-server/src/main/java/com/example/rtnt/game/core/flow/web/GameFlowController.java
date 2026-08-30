package com.example.rtnt.game.core.flow.web;

import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.FlowMode;
import com.example.rtnt.game.core.loop.GameLoop;
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
@RequestMapping("/api/game-flow")
public class GameFlowController {
    private final GameLoop gameLoop;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameFlowController(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public GameFlowDto get() {
        return GameFlowDto.from(this.gameLoop.get());
    }

    @PostMapping("/pause")
    public GameFlowDto pause() {
        return GameFlowDto.from(this.gameLoop.pause());
    }

    @PostMapping("/resume")
    public GameFlowDto resume() {
        return GameFlowDto.from(this.gameLoop.resume());
    }

    @PostMapping("/mode")
    public GameFlowDto setMode(@Valid @RequestBody ModeRequest request) {
        return GameFlowDto.from(this.gameLoop.setMode(request.mode()));
    }

    @PostMapping("/advance")
    public GameFlowDto advance(@Valid @RequestBody AdvanceRequest request) {
        return GameFlowDto.from(this.gameLoop.advance(request.ticks()));
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record GameFlowDto(long tick, FlowMode mode, boolean paused) {
        static GameFlowDto from(GameFlowStatus status) {
            return new GameFlowDto(status.tick(), status.mode(), status.paused());
        }
    }

    public record ModeRequest(@NotNull FlowMode mode) {
    }

    public record AdvanceRequest(@Min(1) @Max(100_000) int ticks) {
    }
}
