package com.example.rtnt.game.loop;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class GameLoop {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameClockMongoRepository gameClockMongoRepository;
    private final GameCommandQueue gameCommandQueue;
    private final WorldSnapshotStore worldSnapshotStore;
    private final int snapshotIntervalTicks;
    private final Object lock = new Object();
    private @Nullable GameClock clock;
    private ClockMode mode = ClockMode.LIVE;
    private boolean paused;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLoop(
            GameClockMongoRepository gameClockMongoRepository,
            GameCommandQueue gameCommandQueue,
            WorldSnapshotStore worldSnapshotStore,
            @Value("${rtnt.clock.snapshot-interval-ticks:1000}") int snapshotIntervalTicks
    ) {
        if (snapshotIntervalTicks < 1) {
            throw new IllegalArgumentException("snapshotIntervalTicks must be at least 1");
        }
        this.gameClockMongoRepository = gameClockMongoRepository;
        this.gameCommandQueue = gameCommandQueue;
        this.worldSnapshotStore = worldSnapshotStore;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
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

    public ClockStatus get() {
        synchronized (this.lock) {
            this.ensureLoaded();
            return this.status();
        }
    }

    public ClockStatus pause() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = true;
            this.persistIfLive();
            log.info("Clock paused at tick {}", this.requireClock().tick());
            return this.status();
        }
    }

    public ClockStatus resume() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = false;
            this.persistIfLive();
            log.info("Clock resumed at tick {}", this.requireClock().tick());
            return this.status();
        }
    }

    public ClockStatus setMode(ClockMode mode) {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.mode = mode;
            this.persistIfLive();
            log.info("Clock mode set to {} at tick {}", mode, this.requireClock().tick());
            return this.status();
        }
    }

    public ClockStatus step() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.execute();
            return this.status();
        }
    }

    public void stepIfLive() {
        synchronized (this.lock) {
            this.ensureLoaded();
            if (this.mode != ClockMode.LIVE || this.paused) {
                return;
            }
            this.execute();
            this.save();
        }
    }

    public ClockStatus advance(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        synchronized (this.lock) {
            this.ensureLoaded();
            for (int i = 0; i < ticks; i++) {
                this.execute();
            }
            log.info("Clock advanced by {} ticks to {}", ticks, this.requireClock().tick());
            return this.status();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private void execute() {
        GameClock current = this.requireClock();
        this.gameCommandQueue.drain(current.tick());
        this.clock = current.advance();
        if (this.requireClock().tick() % this.snapshotIntervalTicks == 0) {
            this.worldSnapshotStore.save(this.requireClock());
        }
    }

    private GameClock requireClock() {
        GameClock current = this.clock;
        if (current == null) {
            throw new IllegalStateException("clock not loaded");
        }
        return current;
    }

    private ClockStatus status() {
        return new ClockStatus(this.requireClock().tick(), this.mode, this.paused);
    }

    private void persistIfLive() {
        if (this.mode == ClockMode.LIVE) {
            this.save();
        }
    }

    private void save() {
        this.gameClockMongoRepository.save(GameClockDocument.from(this.requireClock(), this.mode, this.paused));
    }

    private void ensureLoaded() {
        if (this.clock != null) {
            return;
        }
        this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID)
                .ifPresentOrElse(document -> {
                    this.clock = new GameClock(document.tick());
                    this.mode = document.mode();
                    this.paused = document.paused();
                }, () -> {
                    this.clock = GameClock.initial();
                    this.mode = ClockMode.LIVE;
                    this.paused = false;
                    this.save();
                });
    }
}
