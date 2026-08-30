package com.example.rtnt.game.core.loop;

import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.core.loop.persistence.GameLoopStatusDocument;
import com.example.rtnt.game.core.loop.persistence.GameLoopStatusMongoRepository;
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

    private final GameLoopStatusMongoRepository gameLoopStatusMongoRepository;
    private final GameCommandQueue gameCommandQueue;
    private final WorldSnapshotStore worldSnapshotStore;
    private final IslandService islandService;
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
            GameLoopStatusMongoRepository gameLoopStatusMongoRepository,
            GameCommandQueue gameCommandQueue,
            WorldSnapshotStore worldSnapshotStore,
            IslandService islandService,
            @Value("${rtnt.clock.snapshot-interval-ticks:1000}") int snapshotIntervalTicks
    ) {
        if (snapshotIntervalTicks < 1) {
            throw new IllegalArgumentException("snapshotIntervalTicks must be at least 1");
        }
        this.gameLoopStatusMongoRepository = gameLoopStatusMongoRepository;
        this.gameCommandQueue = gameCommandQueue;
        this.worldSnapshotStore = worldSnapshotStore;
        this.islandService = islandService;
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

    public GameLoopStatus get() {
        synchronized (this.lock) {
            this.ensureLoaded();
            return this.status();
        }
    }

    public GameLoopStatus pause() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = true;
            this.persistIfLive();
            log.info("Clock paused at tick {}", this.requireClock().tick());
            return this.status();
        }
    }

    public GameLoopStatus resume() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = false;
            this.persistIfLive();
            log.info("Clock resumed at tick {}", this.requireClock().tick());
            return this.status();
        }
    }

    public GameLoopStatus setMode(ClockMode mode) {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.mode = mode;
            this.persistIfLive();
            log.info("Clock mode set to {} at tick {}", mode, this.requireClock().tick());
            return this.status();
        }
    }

    public GameLoopStatus step() {
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
        }
    }

    public GameLoopStatus advance(int ticks) {
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

    public void snapshotIfAtTickZero() {
        synchronized (this.lock) {
            this.ensureLoaded();
            if (this.requireClock().tick() == 0) {
                this.persistSnapshot();
            }
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private void execute() {
        GameClock current = this.requireClock();
        boolean eventful = !this.gameCommandQueue.drain(current.tick()).isEmpty();
        this.clock = current.advance();
        boolean snapshotDue = this.requireClock().tick() % this.snapshotIntervalTicks == 0;
        if (snapshotDue) {
            this.persistSnapshot();
        } else if (eventful) {
            this.save();
        }
    }

    private void persistSnapshot() {
        this.worldSnapshotStore.save(this.captureWorld());
        this.save();
    }

    private WorldSnapshot captureWorld() {
        return new WorldSnapshot(this.requireClock().tick(), this.islandService.list());
    }

    private GameClock requireClock() {
        GameClock current = this.clock;
        if (current == null) {
            throw new IllegalStateException("clock not loaded");
        }
        return current;
    }

    private GameLoopStatus status() {
        return new GameLoopStatus(this.requireClock().tick(), this.mode, this.paused);
    }

    private void persistIfLive() {
        if (this.mode == ClockMode.LIVE) {
            this.save();
        }
    }

    private void save() {
        this.gameLoopStatusMongoRepository.save(GameLoopStatusDocument.from(this.requireClock(), this.mode, this.paused));
    }

    private void ensureLoaded() {
        if (this.clock != null) {
            return;
        }
        this.gameLoopStatusMongoRepository.findById(GameLoopStatusDocument.DOCUMENT_ID)
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
