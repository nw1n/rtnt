package com.example.rtnt.game.island.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IslandStatusTest {

    @Test
    void initialPopulationIsZero() {
        IslandStatus status = IslandStatus.initial("island-1");
        assertEquals("island-1", status.islandId());
        assertEquals(0, status.population());
    }

    @Test
    void growIncreasesPopulation() {
        IslandStatus grown = IslandStatus.initial("island-1").grow(4);
        assertEquals(4, grown.population());
        assertEquals("island-1", grown.islandId());
    }
}
