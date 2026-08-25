package com.example.rtnt.service.island;

import com.example.rtnt.domain.island.Island;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandPlacementTest {

    @Test
    void placeIslandKeepsIslandOnTheMap() {
        Island created = new IslandPlacement(2000, 1000).placeIsland("Nassau", List.of());

        assertEquals("Nassau", created.name());
        assertTrue(created.footprint().x() >= 0);
        assertTrue(created.footprint().y() >= 0);
        assertTrue(created.footprint().x() + created.footprint().width() <= 2000);
        assertTrue(created.footprint().y() + created.footprint().length() <= 1000);
    }
}
