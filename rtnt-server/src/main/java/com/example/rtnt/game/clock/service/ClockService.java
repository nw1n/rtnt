package com.example.rtnt.game.clock.service;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import jakarta.annotation.PostConstruct;
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
    private GameClock clock;

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
     * Lifecycle                                                               *
     *                                                                         *
     **************************************************************************/

    @PostConstruct
    void loadOnStartup() {
        synchronized (this.lock) {
            this.ensureLoaded();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock get() {
        synchronized (this.lock) {
            this.ensureLoaded();
            return this.clock;
        }
    }

    public GameClock pause() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.clock = this.clock.pause();
            this.persistIfLive();
            log.info("Clock paused at tick {}", this.clock.tick());
            return this.clock;
        }
    }

    public GameClock resume() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.clock = this.clock.resume();
            this.persistIfLive();
            log.info("Clock resumed at tick {}", this.clock.tick());
            return this.clock;
        }
    }

    public GameClock setMode(ClockMode mode) {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.clock = this.clock.withMode(mode);
            this.persistIfLive();
            log.info("Clock mode set to {} at tick {}", mode, this.clock.tick());
            return this.clock;
        }
    }

    public GameClock advance(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        synchronized (this.lock) {
            this.ensureLoaded();
            for (int i = 0; i < ticks; i++) {
                this.clock = this.clock.advance();
            }
            log.info("Clock advanced by {} ticks to {}", ticks, this.clock.tick());
            return this.clock;
        }
    }

    public void tickIfLive() {
        synchronized (this.lock) {
            this.ensureLoaded();
            if (this.clock.mode() != ClockMode.LIVE || this.clock.paused()) {
                return;
            }
            this.clock = this.clock.advance();
            this.save();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private void ensureLoaded() {
        if (this.clock != null) {
            return;
        }
        this.clock = this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID)
                .map(GameClockDocument::toClock)
                .orElseGet(() -> {
                    GameClock initial = GameClock.initial();
                    this.gameClockMongoRepository.save(GameClockDocument.from(initial));
                    return initial;
                });
    }

    private void persistIfLive() {
        if (this.clock.mode() == ClockMode.LIVE) {
            this.save();
        }
    }

    private void save() {
        this.gameClockMongoRepository.save(GameClockDocument.from(this.clock));
    }
}
