package com.example.rtnt.game.ship.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JourneyTest {

    @Test
    void createStartsActiveWithoutArrival() {
        Journey journey = Journey.create("start", "target", 5, 25);

        assertTrue(journey.active());
        assertNull(journey.arrivedTick());
        assertEquals(5, journey.departedTick());
        assertEquals(25, journey.estimatedArrivalTick());
    }

    @Test
    void completeMarksArrivalAndInactive() {
        Journey completed = Journey.create("start", "target", 5, 25).complete(24);

        assertFalse(completed.active());
        assertEquals(24, completed.arrivedTick());
        assertFalse(Journey.resolveActiveFromStorage(null));
        assertTrue(Journey.resolveActiveFromStorage(true));
    }
}
