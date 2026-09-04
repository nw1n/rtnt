package com.example.rtnt.game.ship.domain;

import com.example.rtnt.game.island.domain.Footprint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipTravelTest {

    @Test
    void ticksAreCeilOfCenterDistanceOverSpeed() {
        Footprint from = new Footprint(0, 0, 10, 10);
        Footprint to = new Footprint(100, 0, 10, 10);

        assertEquals(100.0, ShipTravel.distance(from, to));
        assertEquals(5L, ShipTravel.ticks(from, to, 20));
    }

    @Test
    void ticksAreAtLeastOne() {
        Footprint from = new Footprint(0, 0, 10, 10);
        Footprint to = new Footprint(0, 0, 10, 10);

        assertEquals(1L, ShipTravel.ticks(from, to, 20));
    }
}
