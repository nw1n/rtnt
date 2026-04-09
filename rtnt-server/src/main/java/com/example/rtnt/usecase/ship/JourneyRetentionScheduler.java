package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.ship.JourneyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically trims the journey history so the collection does not grow without bound.
 */
@Component
public class JourneyRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(JourneyRetentionScheduler.class);

    private static final long DEFAULT_INTERVAL_MS = 300_000L; // 5 minutes
    private static final int DEFAULT_MAX_ROWS = 10000; 

    private final JourneyRepository journeyRepository;
    private final int maxRows;

    public JourneyRetentionScheduler(
            JourneyRepository journeyRepository,
            @Value("${rtnt.journeys.retention.max-rows:" + DEFAULT_MAX_ROWS + "}") int maxRows
    ) {
        this.journeyRepository = journeyRepository;
        this.maxRows = maxRows;
    }

    @Scheduled(fixedRateString = "${rtnt.journeys.retention.interval-ms:" + DEFAULT_INTERVAL_MS + "}")
    public void trimExcessJourneys() {
        int removed = this.journeyRepository.trimToMaxSize(this.maxRows);
        if (removed > 0) {
            log.info("Journey retention: removed {} oldest journeys (cap {})", removed, this.maxRows);
        }
    }
}
