package com.example.rtnt.usecase.game;

import com.example.rtnt.adapter.out.persistence.game.GameLoopStateDocument;
import com.example.rtnt.adapter.out.persistence.game.GameLoopStateMongoRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class GameLoopService {

    private static final long DEFAULT_TICK_RATE_MS = 1000L;
    private static final long DEFAULT_PERSIST_RATE_MS = 10_000L;
    private static final String GAME_LOOP_STATE_ID = "game-loop";

    private final AtomicLong tickCounter;
    private final GameLoopStateMongoRepository gameLoopStateMongoRepository;

    public GameLoopService(GameLoopStateMongoRepository gameLoopStateMongoRepository) {
        this.gameLoopStateMongoRepository = gameLoopStateMongoRepository;
        long persistedTick = this.gameLoopStateMongoRepository.findById(GAME_LOOP_STATE_ID)
                .map(GameLoopStateDocument::getCurrentTick)
                .orElse(0L);
        this.tickCounter = new AtomicLong(persistedTick);
    }

    @Scheduled(fixedRateString = "${rtnt.game-loop.tick-rate-ms:" + DEFAULT_TICK_RATE_MS + "}")
    public void scheduledTick() {
        this.tick();
    }

    public long tick() {
        return this.tickCounter.incrementAndGet();
    }

    @Scheduled(fixedRateString = "${rtnt.game-loop.persist-rate-ms:" + DEFAULT_PERSIST_RATE_MS + "}")
    public void scheduledPersist() {
        this.persistCurrentTick();
    }

    @PreDestroy
    public void persistOnShutdown() {
        this.persistCurrentTick();
    }

    public long getCurrentTick() {
        return this.tickCounter.get();
    }

    public synchronized void reset() {
        this.tickCounter.set(0L);
        this.persistCurrentTick();
    }

    private synchronized void persistCurrentTick() {
        long currentTick = this.tickCounter.get();
        this.gameLoopStateMongoRepository.save(new GameLoopStateDocument(GAME_LOOP_STATE_ID, currentTick));
    }
}
