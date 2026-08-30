package com.example.rtnt.game.loop;

import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.service.ClockService;
import org.jspecify.annotations.NullMarked;
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

    private final ClockService clockService;
    private final GameCommandQueue gameCommandQueue;
    private final WorldSnapshotStore worldSnapshotStore;
    private final int snapshotIntervalTicks;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public GameLoop(
            ClockService clockService,
            GameCommandQueue gameCommandQueue,
            WorldSnapshotStore worldSnapshotStore,
            @Value("${rtnt.clock.snapshot-interval-ticks:1000}") int snapshotIntervalTicks
    ) {
        if (snapshotIntervalTicks < 1) {
            throw new IllegalArgumentException("snapshotIntervalTicks must be at least 1");
        }
        this.clockService = clockService;
        this.gameCommandQueue = gameCommandQueue;
        this.worldSnapshotStore = worldSnapshotStore;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock step() {
        return this.clockService.runStep(this::execute);
    }

    public void stepIfLive() {
        this.clockService.runLiveStep(this::execute);
    }

    public GameClock advance(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
        GameClock clock = this.clockService.get();
        for (int i = 0; i < ticks; i++) {
            clock = this.step();
        }
        log.info("Clock advanced by {} ticks to {}", ticks, clock.tick());
        return clock;
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
}
