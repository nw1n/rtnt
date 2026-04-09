package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.ship.ShipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds ships on first run (empty store). Does not delete existing data.
 */
@Component
@Order(2)
public class ShipStartupInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShipStartupInitializer.class);
    private static final int DEFAULT_SHIP_COUNT = 15;

    private final ShipRepository shipRepository;
    private final ShipFactory shipFactory;
    private final int shipCount;

    public ShipStartupInitializer(
            ShipRepository shipRepository,
            ShipFactory shipFactory,
            @Value("${rtnt.startup.ship-count:" + DEFAULT_SHIP_COUNT + "}") int shipCount
    ) {
        if (shipCount < 0) {
            throw new IllegalArgumentException("rtnt.startup.ship-count cannot be negative");
        }
        this.shipRepository = shipRepository;
        this.shipFactory = shipFactory;
        this.shipCount = shipCount;
    }

    private void seedShipsIfEmpty() {
        long existing = this.shipRepository.count();
        if (existing > 0) {
            log.info("Skipping ship seed: {} ships already in database", existing);
            return;
        }
        for (int i = 0; i < this.shipCount; i++) {
            this.shipRepository.save(this.shipFactory.createWithCaribbeanName());
        }
        log.info("Startup seed completed: {} ships created", this.shipCount);
    }

    @Override
    public void run(String... args) {
        this.seedShipsIfEmpty();
    }
}
