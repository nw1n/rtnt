package com.example.rtnt.game.island.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IslandNamesTest {

    @Test
    void nextUsesCatalogThenFallsBackToNumberedNames() {
        IslandNames islandNames = new IslandNames(List.of("Nassau", "Havana"));

        assertEquals("Nassau", islandNames.next());
        assertEquals("Havana", islandNames.next());
        assertEquals("Island 3", islandNames.next());
    }
}
