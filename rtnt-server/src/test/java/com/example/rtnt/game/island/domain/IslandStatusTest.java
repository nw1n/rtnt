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
}
