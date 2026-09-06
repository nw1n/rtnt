package com.example.rtnt.game.island.service;

import com.example.rtnt.game.core.event.IslandCreated;
import com.example.rtnt.game.core.event.IslandPopulationGrew;
import com.example.rtnt.game.core.event.World;
import com.example.rtnt.game.core.event.WorldEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandPopulationGrowthTest {

    @Test
    void skipsTicksOffInterval() {
        IslandPopulationGrowth growth = this.growth(1.0, new Random(1));
        World world = World.empty().apply(new IslandCreated(0, "a", "A", 0, 0, 10, 10));

        assertTrue(growth.decide(world, 0).isEmpty());
        assertTrue(growth.decide(world, 9).isEmpty());
    }

    @Test
    void growsWhenChanceIsCertain() {
        World world = World.empty().apply(new IslandCreated(0, "a", "A", 0, 0, 10, 10));
        IslandPopulationGrowth growth = this.growth(1.0, new Random(1));

        List<WorldEvent> events = growth.decide(world, 10);

        assertEquals(1, events.size());
        IslandPopulationGrew grew = (IslandPopulationGrew) events.getFirst();
        assertEquals("a", grew.islandId());
        assertEquals(10, grew.tick());
        assertTrue(grew.amount() >= 1);
    }

    @Test
    void doesNotGrowWhenChanceIsZero() {
        World world = World.empty().apply(new IslandCreated(0, "a", "A", 0, 0, 10, 10));
        IslandPopulationGrowth growth = this.growth(0.0, new Random(1));

        assertTrue(growth.decide(world, 10).isEmpty());
    }

    private IslandPopulationGrowth growth(double chance, Random random) {
        return new IslandPopulationGrowth(10, chance, 1, 3, random);
    }
}
