package com.example.rtnt.adapter.in.web;

import com.example.rtnt.usecase.game.GameLoopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameLoopService gameLoopService;

    public GameController(GameLoopService gameLoopService) {
        this.gameLoopService = gameLoopService;
    }

    @GetMapping("/ticks")
    public TickResponse getTicks() {
        return new TickResponse(this.gameLoopService.getCurrentTick());
    }

    public record TickResponse(long tickCount) {
    }
}
