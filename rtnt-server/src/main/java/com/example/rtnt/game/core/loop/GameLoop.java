package com.example.rtnt.game.core.loop;

import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.GameTick;
import com.example.rtnt.game.core.flow.TimeMode;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusDocument;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusMongoRepository;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.island.service.IslandService;
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

    private final GameFlowStatusMongoRepository gameFlowStatusMongoRepository;
    private final GameCommandQueue gameCommandQueue;
    private final WorldSnapshotStore worldSnapshotStore;
    private final IslandService islandService;
    private final int snapshotIntervalTicks;
    private final Object lock = new Object();
    private @Nullable GameTick gameTick;
    private TimeMode mode = TimeMode.LIVE;
    private boolean paused;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLoop(
            GameFlowStatusMongoRepository gameFlowStatusMongoRepository,
            GameCommandQueue gameCommandQueue,
            WorldSnapshotStore worldSnapshotStore,
            IslandService islandService,
            @Value("${rtnt.snapshot.interval-ticks:1000}") int snapshotIntervalTicks
    ) {
        if (snapshotIntervalTicks < 1) {
            throw new IllegalArgumentException("snapshotIntervalTicks must be at least 1");
        }
        this.gameFlowStatusMongoRepository = gameFlowStatusMongoRepository;
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

    public GameFlowStatus get() {
        synchronized (this.lock) {
            this.ensureLoaded();
            return this.status();
        }
    }

    public GameFlowStatus pause() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = true;
            this.persistIfLive();
            log.info("Game flow paused at tick {}", this.requireTick().tick());
            return this.status();
        }
    }

    public GameFlowStatus resume() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = false;
            this.persistIfLive();
            log.info("Game flow resumed at tick {}", this.requireTick().tick());
            return this.status();
        }
    }

    public GameFlowStatus setMode(TimeMode mode) {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.mode = mode;
            this.persistIfLive();
            log.info("Time mode set to {} at tick {}", mode, this.requireTick().tick());
            return this.status();
        }
    }

    public GameFlowStatus step() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.execute();
            return this.status();
        }
    }

    public void stepIfLive() {
        synchronized (this.lock) {
            this.ensureLoaded();
            if (this.mode != TimeMode.LIVE || this.paused) {
                return;
            }
            this.execute();
        }
    }

    public GameFlowStatus advance(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        synchronized (this.lock) {
            this.ensureLoaded();
            for (int i = 0; i < ticks; i++) {
                this.execute();
            }
            log.info("Game flow advanced by {} ticks to {}", ticks, this.requireTick().tick());
            return this.status();
        }
    }

    public void snapshotIfAtTickZero() {
        synchronized (this.lock) {
            this.ensureLoaded();
            if (this.requireTick().tick() == 0 && !this.worldSnapshotStore.exists(0)) {
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
        GameTick current = this.requireTick();
        boolean eventful = !this.gameCommandQueue.drain(current.tick()).isEmpty();
        this.gameTick = current.advance();
        boolean snapshotDue = this.requireTick().tick() % this.snapshotIntervalTicks == 0;
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
        return new WorldSnapshot(this.requireTick().tick(), this.islandService.list());
    }

    private GameTick requireTick() {
        GameTick current = this.gameTick;
        if (current == null) {
            throw new IllegalStateException("tick not loaded");
        }
        return current;
    }

    private GameFlowStatus status() {
        return new GameFlowStatus(this.requireTick().tick(), this.mode, this.paused);
    }

    private void persistIfLive() {
        if (this.mode == TimeMode.LIVE) {
            this.save();
        }
    }

    private void save() {
        this.gameFlowStatusMongoRepository.save(GameFlowStatusDocument.from(this.requireTick(), this.mode, this.paused));
    }

    private void ensureLoaded() {
        if (this.gameTick != null) {
            return;
        }
        this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID)
                .ifPresentOrElse(document -> {
                    this.gameTick = new GameTick(document.tick());
                    this.mode = document.mode();
                    this.paused = document.paused();
                }, () -> {
                    this.gameTick = GameTick.initial();
                    this.mode = TimeMode.LIVE;
                    this.paused = false;
                    this.save();
                });
    }
}
