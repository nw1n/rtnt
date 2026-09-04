package com.example.rtnt.game.ship.service;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ShipSeeder implements ApplicationRunner {
    private final ShipService shipService;

    public ShipSeeder(ShipService shipService) {
        this.shipService = shipService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        this.shipService.seedIfEmpty();
    }
}
