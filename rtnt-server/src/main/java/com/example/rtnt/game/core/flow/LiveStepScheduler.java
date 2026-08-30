package com.example.rtnt.game.core.flow;

import com.example.rtnt.game.core.loop.GameLoop;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LiveStepScheduler {
    private final GameLoop gameLoop;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public LiveStepScheduler(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    /***************************************************************************
     *                                                                         *
     * Scheduled                                                               *
     *                                                                         *
     **************************************************************************/

    @Scheduled(fixedRateString = "${rtnt.flow.live-interval-ms:1000}")
    public void onInterval() {
        this.gameLoop.stepIfLive();
    }
}
