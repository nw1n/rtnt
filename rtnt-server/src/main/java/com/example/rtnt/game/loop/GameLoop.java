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

    public GameClock get() {
        synchronized (this.lock) {
            return this.ensureLoaded();
        }
    }

    public GameClock pause() {
        synchronized (this.lock) {
            this.clock = this.ensureLoaded().pause();
            this.persistIfLive();
            log.info("Clock paused at tick {}", this.clock.tick());
            return this.clock;
        }
    }

    public GameClock resume() {
        synchronized (this.lock) {
            this.clock = this.ensureLoaded().resume();
            this.persistIfLive();
            log.info("Clock resumed at tick {}", this.clock.tick());
            return this.clock;
        }
    }

    public GameClock setMode(ClockMode mode) {
        synchronized (this.lock) {
            this.clock = this.ensureLoaded().withMode(mode);
            this.persistIfLive();
            log.info("Clock mode set to {} at tick {}", mode, this.clock.tick());
            return this.clock;
        }
    }

    public GameClock step() {
        synchronized (this.lock) {
            this.clock = this.execute(this.ensureLoaded());
            return this.clock;
        }
    }

    public void stepIfLive() {
        synchronized (this.lock) {
            GameClock current = this.ensureLoaded();
            if (current.mode() != ClockMode.LIVE || current.paused()) {
                return;
            }
            this.clock = this.execute(current);
            this.save();
        }
    }

    public GameClock advance(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        synchronized (this.lock) {
            GameClock current = this.ensureLoaded();
            for (int i = 0; i < ticks; i++) {
                current = this.execute(current);
            }
            this.clock = current;
            log.info("Clock advanced by {} ticks to {}", ticks, current.tick());
            return current;
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private GameClock execute(GameClock clock) {
        this.gameCommandQueue.drain(clock.tick());
        GameClock next = clock.advance();
        if (next.tick() % this.snapshotIntervalTicks == 0) {
            this.worldSnapshotStore.save(next);
        }
        return next;
    }

    private GameClock ensureLoaded() {
        if (this.clock != null) {
            return this.clock;
        }
        this.clock = this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID)
                .map(GameClockDocument::toClock)
                .orElseGet(() -> {
                    GameClock initial = GameClock.initial();
                    this.gameClockMongoRepository.save(GameClockDocument.from(initial));
                    return initial;
                });
        return this.clock;
    }

    private void persistIfLive() {
        if (this.clock != null && this.clock.mode() == ClockMode.LIVE) {
            this.save();
        }
    }

    private void save() {
        if (this.clock != null) {
            this.gameClockMongoRepository.save(GameClockDocument.from(this.clock));
        }
    }
}
