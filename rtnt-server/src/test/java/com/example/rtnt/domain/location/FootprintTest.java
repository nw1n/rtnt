package com.example.rtnt.domain.location;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootprintTest {

    @Test
    void testFootprintCreation() {
        Footprint footprint = Footprint.create(5, 10, 20, 30);
        assertEquals(5, footprint.getX());
        assertEquals(10, footprint.getY());
        assertEquals(20, footprint.getWidth());
        assertEquals(30, footprint.getLength());
    }

    @Test
    void testOverlapWithDistanceBuffer() {
        Footprint footprint1 = Footprint.create(0, 0, 10, 10);
        Footprint footprint2 = Footprint.create(15, 0, 10, 10);
        assertTrue(footprint1.overlapsOrTooClose(footprint2, 5));
    }
}
