package com.example.rtnt.game.ship.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipNamesTest {

    @Test
    void nextUsesCatalogThenFallsBackToNumberedNames() {
        ShipNames shipNames = new ShipNames(List.of("Black Pearl", "Revenge"));

        assertEquals("Black Pearl", shipNames.next());
        assertEquals("Revenge", shipNames.next());
        assertEquals("Ship 3", shipNames.next());
    }
}
