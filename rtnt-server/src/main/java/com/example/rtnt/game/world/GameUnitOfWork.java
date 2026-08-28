package com.example.rtnt.game.world;

import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import com.example.rtnt.game.world.persistence.GameLogDocument;
import com.example.rtnt.game.world.persistence.GameLogMongoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameUnitOfWork {
    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameClockMongoRepository gameClockMongoRepository;
    private final GameLogMongoRepository gameLogMongoRepository;
    private GameClock clock;
    private boolean clockDirty;
    private final List<GameLogEvent> pendingLog = new ArrayList<>();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameUnitOfWork(
            GameClockMongoRepository gameClockMongoRepository,
            GameLogMongoRepository gameLogMongoRepository
    ) {
        this.gameClockMongoRepository = gameClockMongoRepository;
        this.gameLogMongoRepository = gameLogMongoRepository;
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
        this.pendingLog.add(event);
    }

    public void flush() {
        if (this.clockDirty && this.clock != null) {
            this.gameClockMongoRepository.save(GameClockDocument.from(this.clock));
            this.clockDirty = false;
        }
        if (!this.pendingLog.isEmpty()) {
            this.gameLogMongoRepository.saveAll(this.pendingLog.stream().map(GameLogDocument::from).toList());
            this.pendingLog.clear();
        }
    }
}
