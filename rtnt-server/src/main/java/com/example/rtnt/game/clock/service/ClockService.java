package com.example.rtnt.game.clock.service;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.event.ClockModeChangedEvent;
import com.example.rtnt.game.clock.event.ClockPausedEvent;
import com.example.rtnt.game.clock.event.ClockResumedEvent;
import com.example.rtnt.game.clock.event.TickAdvancedEvent;
import com.example.rtnt.game.system.GameSystem;
import com.example.rtnt.game.system.GameUnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.UnaryOperator;

@Service
public class ClockService {
    private static final Logger log = LoggerFactory.getLogger(ClockService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameUnitOfWork unitOfWork;
    private final List<GameSystem> gameSystems;
    private final ApplicationEventPublisher eventPublisher;
    private final int batchFlushEveryTicks;
    private final Object lock = new Object();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ClockService(
            GameUnitOfWork unitOfWork,
            List<GameSystem> gameSystems,
            ApplicationEventPublisher eventPublisher,
            @Value("${rtnt.clock.batch-flush-every-ticks}") int batchFlushEveryTicks
    ) {
        if (batchFlushEveryTicks < 1) {
            throw new IllegalArgumentException("batchFlushEveryTicks must be at least 1");
        }
        this.unitOfWork = unitOfWork;
        this.gameSystems = List.copyOf(gameSystems);
        this.eventPublisher = eventPublisher;
        this.batchFlushEveryTicks = batchFlushEveryTicks;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameClock get() {
        synchronized (this.lock) {
            return this.unitOfWork.clock();
        }
    }

    public GameClock pause() {
        synchronized (this.lock) {
            GameClock clock = this.mutateClock(GameClock::pause);
            this.unitOfWork.flush();
            this.eventPublisher.publishEvent(new ClockPausedEvent(clock));
            log.info("Clock paused at tick {}", clock.tick());
            return clock;
        }
    }

    public GameClock resume() {
        synchronized (this.lock) {
            GameClock clock = this.mutateClock(GameClock::resume);
            this.unitOfWork.flush();
            this.eventPublisher.publishEvent(new ClockResumedEvent(clock));
            log.info("Clock resumed at tick {}", clock.tick());
            return clock;
        }
    }

    public GameClock setMode(ClockMode mode) {
        synchronized (this.lock) {
            GameClock clock = this.mutateClock(current -> current.withMode(mode));
            this.unitOfWork.flush();
            this.eventPublisher.publishEvent(new ClockModeChangedEvent(clock));
            log.info("Clock mode set to {} at tick {}", mode, clock.tick());
            return clock;
        }
    }

    public GameClock advance(int ticks) {
        synchronized (this.lock) {
            if (ticks < 1) {
                throw new IllegalArgumentException("ticks must be at least 1");
            }
            GameClock clock = this.unitOfWork.clock();
            for (int i = 1; i <= ticks; i++) {
                clock = this.applyTick(clock);
                if (i % this.batchFlushEveryTicks == 0) {
                    this.unitOfWork.flush();
                }
            }
            this.unitOfWork.flush();
            log.info("Clock advanced by {} ticks to {}", ticks, clock.tick());
            return clock;
        }
    }

    public void tickIfLive() {
        synchronized (this.lock) {
            GameClock clock = this.unitOfWork.clock();
            if (clock.mode() != ClockMode.LIVE || clock.paused()) {
                return;
            }
            this.applyTick(clock);
            this.unitOfWork.flush();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private GameClock mutateClock(UnaryOperator<GameClock> mutation) {
        GameClock clock = mutation.apply(this.unitOfWork.clock());
        this.unitOfWork.replaceClock(clock);
        return clock;
    }

    private GameClock applyTick(GameClock current) {
        GameClock next = current.advance();
        this.unitOfWork.replaceClock(next);
        for (GameSystem system : this.gameSystems) {
            system.onTick(next, this.unitOfWork);
        }
        this.eventPublisher.publishEvent(new TickAdvancedEvent(next));
        return next;
    }
}
