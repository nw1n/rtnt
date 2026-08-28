package com.example.rtnt.game.log.service;

import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.persistence.GameLogDocument;
import com.example.rtnt.game.log.persistence.GameLogMongoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameLogService {
    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameLogMongoRepository gameLogMongoRepository;
    private final List<GameLogEvent> pending = new ArrayList<>();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLogService(GameLogMongoRepository gameLogMongoRepository) {
        this.gameLogMongoRepository = gameLogMongoRepository;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public void append(GameLogEvent event) {
        this.pending.add(event);
    }

    public List<GameLogEvent> listRecent() {
        return this.gameLogMongoRepository.findTop200ByOrderByTickDesc().stream()
                .map(GameLogDocument::toEvent)
                .toList();
    }

    public void flush() {
        if (this.pending.isEmpty()) {
            return;
        }
        this.gameLogMongoRepository.saveAll(this.pending.stream().map(GameLogDocument::from).toList());
        this.pending.clear();
    }
}
