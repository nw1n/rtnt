package com.example.rtnt.game.core.loop;

import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.FlowMode;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusDocument;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusMongoRepository;
import com.example.rtnt.game.core.ticker.GameTick;
import com.example.rtnt.game.core.ticker.persistence.TickerDocument;
import com.example.rtnt.game.core.ticker.persistence.TickerMongoRepository;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final TickerMongoRepository tickerMongoRepository;
    private final GameFlowStatusMongoRepository gameFlowStatusMongoRepository;
    private final GameCommandQueue gameCommandQueue;
    private final Object lock = new Object();
    private @Nullable GameTick gameTick;
    private FlowMode mode = FlowMode.BATCH;
    private boolean paused = true;
    private boolean loaded;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLoop(
            TickerMongoRepository tickerMongoRepository,
            GameFlowStatusMongoRepository gameFlowStatusMongoRepository,
            GameCommandQueue gameCommandQueue
    ) {
        this.tickerMongoRepository = tickerMongoRepository;
        this.gameFlowStatusMongoRepository = gameFlowStatusMongoRepository;
        this.gameCommandQueue = gameCommandQueue;
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

    public GameFlowStatus step() {
        synchronized (this.lock) {
            this.ensureLoaded();
            this.execute();
            this.saveTick();
            return this.status();
        }
    }

    public void stepIfLive() {
        synchronized (this.lock) {
            this.ensureLoaded();
            if (this.mode != FlowMode.LIVE || this.paused) {
                return;
            }
            this.execute();
            this.saveTick();
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
            this.saveTick();
            log.info("Game flow advanced by {} ticks to {}", ticks, this.requireTick().tick());
            return this.status();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private void execute() {
        GameTick current = this.requireTick();
        this.gameCommandQueue.drain(current.tick());
        this.gameTick = current.advance();
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

    private void persistFlowIfLive() {
        if (this.mode == FlowMode.LIVE) {
            this.saveFlow();
        }
    }

    private void saveTick() {
        this.tickerMongoRepository.save(TickerDocument.from(this.requireTick()));
    }

    private void saveFlow() {
        this.gameFlowStatusMongoRepository.save(GameFlowStatusDocument.from(this.mode, this.paused));
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
                }, () -> {
                    this.mode = FlowMode.BATCH;
                    this.paused = true;
                    this.saveFlow();
                });
        this.loaded = true;
    }
}
