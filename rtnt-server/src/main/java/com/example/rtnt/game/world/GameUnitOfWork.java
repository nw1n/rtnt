package com.example.rtnt.game.world;

import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.service.GameLogService;
import org.springframework.stereotype.Component;

@Component
public class GameUnitOfWork {
    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameClockMongoRepository gameClockMongoRepository;
    private final GameLogService gameLogService;
    private GameClock clock;
    private boolean clockDirty;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameUnitOfWork(
            GameClockMongoRepository gameClockMongoRepository,
            GameLogService gameLogService
    ) {
        this.gameClockMongoRepository = gameClockMongoRepository;
        this.gameLogService = gameLogService;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock clock() {
        if (this.clock == null) {
            this.clock = this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID)
                    .map(GameClockDocument::toClock)
                    .orElseGet(() -> {
                        GameClock initial = GameClock.initial();
                        this.clock = initial;
                        this.clockDirty = true;
                        this.flush();
                        return initial;
                    });
        }
        return this.clock;
    }

    public void replaceClock(GameClock clock) {
        this.clock = clock;
        this.clockDirty = true;
    }

    public void append(GameLogEvent event) {
        this.gameLogService.append(event);
    }

    public void flush() {
        if (this.clockDirty && this.clock != null) {
            this.gameClockMongoRepository.save(GameClockDocument.from(this.clock));
            this.clockDirty = false;
        }
        this.gameLogService.flush();
    }
}
