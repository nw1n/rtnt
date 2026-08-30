package com.example.rtnt.game.clock.service;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClockService {
    private static final Logger log = LoggerFactory.getLogger(ClockService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameClockMongoRepository gameClockMongoRepository;
    private final Object lock = new Object();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ClockService(GameClockMongoRepository gameClockMongoRepository) {
        this.gameClockMongoRepository = gameClockMongoRepository;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock get() {
        synchronized (this.lock) {
            return this.load();
        }
    }

    public GameClock pause() {
        synchronized (this.lock) {
            GameClock clock = this.load().pause();
            this.save(clock);
            log.info("Clock paused at tick {}", clock.tick());
            return clock;
        }
    }

    public GameClock resume() {
        synchronized (this.lock) {
            GameClock clock = this.load().resume();
            this.save(clock);
            log.info("Clock resumed at tick {}", clock.tick());
            return clock;
        }
    }

    public GameClock setMode(ClockMode mode) {
        synchronized (this.lock) {
            GameClock clock = this.load().withMode(mode);
            this.save(clock);
            log.info("Clock mode set to {} at tick {}", mode, clock.tick());
            return clock;
        }
    }

    public GameClock advance(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        synchronized (this.lock) {
            GameClock clock = this.load();
            for (int i = 0; i < ticks; i++) {
                clock = clock.advance();
            }
            this.save(clock);
            log.info("Clock advanced by {} ticks to {}", ticks, clock.tick());
            return clock;
        }
    }

    public void tickIfLive() {
        synchronized (this.lock) {
            GameClock clock = this.load();
            if (clock.mode() != ClockMode.LIVE || clock.paused()) {
                return;
            }
            this.save(clock.advance());
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private GameClock load() {
        return this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID)
                .map(GameClockDocument::toClock)
                .orElseGet(() -> {
                    GameClock initial = GameClock.initial();
                    this.save(initial);
                    return initial;
                });
    }

    private void save(GameClock clock) {
        this.gameClockMongoRepository.save(GameClockDocument.from(clock));
    }
}
