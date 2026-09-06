package com.example.rtnt.game.core.event;

import com.example.rtnt.game.island.domain.IslandStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest {

    @Test
    void createdIslandStartsWithZeroPopulation() {
        IslandCreated created = new IslandCreated(0, "i1", "North", 1, 2, 10, 12);

        World world = World.empty().apply(created);

        assertEquals(1, world.islands().size());
        assertEquals("North", world.islands().getFirst().name());
        assertEquals(List.of(IslandStatus.initial("i1")), world.islandStatuses());
    }

    @Test
    void growthIsDeterministicFromRecordedAmount() {
        World world = World.empty().applyAll(List.of(
                new IslandCreated(0, "i1", "North", 1, 2, 10, 12),
                new IslandPopulationGrew(10, "i1", 3),
                new IslandPopulationGrew(20, "i1", 2)
        ));

        assertEquals(5, world.islandStatuses().getFirst().population());
    }

    @Test
    void worldClearedRemovesIslands() {
        World world = World.empty().applyAll(List.of(
                new IslandCreated(0, "i1", "North", 1, 2, 10, 12),
                new WorldCleared(5),
                new IslandCreated(5, "i2", "South", 3, 4, 8, 9)
        ));

        assertEquals(1, world.islands().size());
        assertEquals("i2", world.islands().getFirst().id());
        assertTrue(world.islandStatuses().stream().noneMatch(status -> status.islandId().equals("i1")));
    }
}
