package com.example.rtnt.game.core.loop;

import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.FlowMode;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusDocument;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusMongoRepository;
import com.example.rtnt.game.core.ticker.GameTick;
import com.example.rtnt.game.core.ticker.persistence.TickerDocument;
import com.example.rtnt.game.core.ticker.persistence.TickerMongoRepository;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.island.service.IslandPopulationGrowth;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.service.ShipJourneyCheck;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@NullMarked
public class GameLoop {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final TickerMongoRepository tickerMongoRepository;
    private final GameFlowStatusMongoRepository gameFlowStatusMongoRepository;
    private final GameCommandQueue gameCommandQueue;
    private final WorldSnapshotStore worldSnapshotStore;
    private final IslandService islandService;
    private final IslandPopulationGrowth islandPopulationGrowth;
    private final ShipJourneyCheck shipJourneyCheck;
    private final int snapshotIntervalTicks;
    private final int defaultLiveIntervalMs;
    private final Object lock = new Object();
    private @Nullable GameTick gameTick;
    private FlowMode mode = FlowMode.BATCH;
    private boolean paused = true;
    private int liveIntervalMs;
    private long lastLiveStepAtMs;
    private boolean loaded;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLoop(
            TickerMongoRepository tickerMongoRepository,
            GameFlowStatusMongoRepository gameFlowStatusMongoRepository,
            GameCommandQueue gameCommandQueue,
            WorldSnapshotStore worldSnapshotStore,
            IslandService islandService,
            IslandPopulationGrowth islandPopulationGrowth,
            ShipJourneyCheck shipJourneyCheck,
            @Value("${rtnt.snapshot.interval-ticks:1000}") int snapshotIntervalTicks,
            @Value("${rtnt.flow.live-interval-ms:1000}") int liveIntervalMs
    ) {
        if (snapshotIntervalTicks < 1) {
            throw new IllegalArgumentException("snapshotIntervalTicks must be at least 1");
        }
        if (liveIntervalMs < 1) {
            throw new IllegalArgumentException("liveIntervalMs must be at least 1");
        }
        this.tickerMongoRepository = tickerMongoRepository;
        this.gameFlowStatusMongoRepository = gameFlowStatusMongoRepository;
        this.gameCommandQueue = gameCommandQueue;
        this.worldSnapshotStore = worldSnapshotStore;
        this.islandService = islandService;
        this.islandPopulationGrowth = islandPopulationGrowth;
        this.shipJourneyCheck = shipJourneyCheck;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
        this.defaultLiveIntervalMs = liveIntervalMs;
        this.liveIntervalMs = liveIntervalMs;
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
            this.persistFlowIfLive();
            log.info("Game flow paused at tick {}", this.requireTick().tick());
            return this.status();
        }
    }

    public GameFlowStatus resume() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.paused = false;
            this.persistFlowIfLive();
            log.info("Game flow resumed at tick {}", this.requireTick().tick());
            return this.status();
        }
    }

    public GameFlowStatus setMode(FlowMode mode) {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.mode = mode;
            if (mode == FlowMode.LIVE) {
                this.paused = false;
            }
            this.persistFlowIfLive();
            log.info("Flow mode set to {} at tick {}", mode, this.requireTick().tick());
            return this.status();
        }
    }

    public GameFlowStatus setLiveIntervalMs(int milliseconds) {
        if (milliseconds < 1 || milliseconds > 60_000) {
            throw new IllegalArgumentException("liveIntervalMs must be between 1 and 60000");
        }
        synchronized (this.lock) {
            this.ensureLoaded();
            this.liveIntervalMs = milliseconds;
            this.saveFlow();
            log.info("Live interval set to {} ms at tick {}", milliseconds, this.requireTick().tick());
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
            if (this.mode != FlowMode.LIVE || this.paused) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - this.lastLiveStepAtMs < this.liveIntervalMs) {
                return;
            }
            this.lastLiveStepAtMs = now;
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

    public GameFlowStatus loadFromSnapshot(long tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("tick must be >= 0");
        }
        synchronized (this.lock) {
            this.ensureLoaded();
            WorldSnapshot snapshot = this.worldSnapshotStore.findByTick(tick)
                    .orElseThrow(() -> new NoSuchElementException("snapshot not found for tick " + tick));
            this.islandService.replaceAll(snapshot.islands(), snapshot.islandStatuses());
            this.gameCommandQueue.clear();
            this.gameTick = new GameTick(snapshot.tick());
            this.saveTick();
            this.paused = true;
            this.persistFlowIfLive();
            log.info("Loaded world snapshot at tick {}", snapshot.tick());
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
        if (this.islandPopulationGrowth.applyIfDue(this.requireTick().tick())) {
            eventful = true;
        }
        if (this.shipJourneyCheck.applyIfDue(this.requireTick().tick())) {
            eventful = true;
        }
        boolean snapshotDue = this.requireTick().tick() % this.snapshotIntervalTicks == 0;
        if (snapshotDue) {
            this.persistSnapshot();
        } else if (eventful) {
            this.saveTick();
        }
    }

    private void persistSnapshot() {
        this.worldSnapshotStore.save(this.captureWorld());
        this.saveTick();
    }

    private WorldSnapshot captureWorld() {
        return new WorldSnapshot(
                this.requireTick().tick(),
                this.islandService.list(),
                this.islandService.listStatuses()
        );
    }

    private GameTick requireTick() {
        GameTick current = this.gameTick;
        if (current == null) {
            throw new IllegalStateException("tick not loaded");
        }
        return current;
    }

    private GameFlowStatus status() {
        return new GameFlowStatus(this.requireTick().tick(), this.mode, this.paused, this.liveIntervalMs);
    }

    private void persistFlowIfLive() {
        if (this.mode == FlowMode.LIVE) {
            this.saveFlow();
        }
    }

    private void saveTick() {
        this.tickerMongoRepository.save(TickerDocument.from(this.requireTick()));
    }

    private void saveFlow() {
        this.gameFlowStatusMongoRepository.save(
                GameFlowStatusDocument.from(this.mode, this.paused, this.liveIntervalMs)
        );
    }

    private void ensureLoaded() {
        if (this.loaded) {
            return;
        }
        this.gameTick = this.tickerMongoRepository.findById(TickerDocument.DOCUMENT_ID)
                .map(document -> new GameTick(document.tick()))
                .orElseGet(() -> {
                    GameTick initial = GameTick.initial();
                    this.tickerMongoRepository.save(TickerDocument.from(initial));
                    return initial;
                });
        this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID)
                .ifPresentOrElse(document -> {
                    this.mode = document.mode();
                    this.paused = document.paused();
                    this.liveIntervalMs = document.resolvedLiveIntervalMs(this.defaultLiveIntervalMs);
                }, () -> {
                    this.mode = FlowMode.BATCH;
                    this.paused = true;
                    this.liveIntervalMs = this.defaultLiveIntervalMs;
                    this.saveFlow();
                });
        this.loaded = true;
    }
}
