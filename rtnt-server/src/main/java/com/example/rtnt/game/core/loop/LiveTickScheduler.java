package com.example.rtnt.game.core.loop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rtnt.clock.live-enabled", havingValue = "true", matchIfMissing = true)
public class LiveTickScheduler {
    private final GameLoop gameLoop;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public LiveTickScheduler(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    /***************************************************************************
     *                                                                         *
     * Scheduled                                                               *
     *                                                                         *
     **************************************************************************/

    @Scheduled(fixedRateString = "${rtnt.clock.live-interval-ms:1000}")
    public void onInterval() {
        this.gameLoop.stepIfLive();
    }
}
