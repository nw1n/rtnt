package com.example.rtnt.game.log.web;

import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.service.GameLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class GameLogController {
    private final GameLogService gameLogService;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLogController(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public List<GameLogDto> list() {
        return this.gameLogService.listRecent().stream()
                .map(GameLogDto::from)
                .toList();
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record GameLogDto(long tick, String type, String detail) {
        static GameLogDto from(GameLogEvent event) {
            return new GameLogDto(event.tick(), event.type(), event.detail());
        }
    }
}
