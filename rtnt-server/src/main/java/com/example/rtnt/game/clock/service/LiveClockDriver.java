package com.example.rtnt.game.clock.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rtnt.clock.live-enabled", havingValue = "true", matchIfMissing = true)
public class LiveClockDriver {
    private final ClockService clockService;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public LiveClockDriver(ClockService clockService) {
        this.clockService = clockService;
    }

    /***************************************************************************
     *                                                                         *
     * Scheduled                                                               *
     *                                                                         *
     **************************************************************************/

    @Scheduled(fixedRateString = "${rtnt.clock.live-interval-ms:1000}")
    public void onInterval() {
        this.clockService.tickIfLive();
    }
}
