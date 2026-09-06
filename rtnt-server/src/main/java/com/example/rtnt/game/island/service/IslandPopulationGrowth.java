package com.example.rtnt.game.island.service;

import com.example.rtnt.game.core.event.IslandPopulationGrew;
import com.example.rtnt.game.core.event.World;
import com.example.rtnt.game.core.event.WorldEvent;
import com.example.rtnt.game.island.domain.IslandStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@NullMarked
public class IslandPopulationGrowth {
    private final int checkIntervalTicks;
    private final double growthChance;
    private final int growthMin;
    private final int growthMax;
    private final Random random;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    @Autowired
    public IslandPopulationGrowth(
            @Value("${rtnt.island.population-check-interval-ticks:100}") int checkIntervalTicks,
            @Value("${rtnt.island.population-growth-chance:0.25}") double growthChance,
            @Value("${rtnt.island.population-growth-min:1}") int growthMin,
            @Value("${rtnt.island.population-growth-max:3}") int growthMax
    ) {
        this(checkIntervalTicks, growthChance, growthMin, growthMax, new Random());
    }

    IslandPopulationGrowth(
            int checkIntervalTicks,
            double growthChance,
            int growthMin,
            int growthMax,
            Random random
    ) {
        if (checkIntervalTicks < 1) {
            throw new IllegalArgumentException("checkIntervalTicks must be at least 1");
        }
        if (growthChance < 0 || growthChance > 1) {
            throw new IllegalArgumentException("growthChance must be between 0 and 1");
        }
        if (growthMin < 1) {
            throw new IllegalArgumentException("growthMin must be at least 1");
        }
        if (growthMax < growthMin) {
            throw new IllegalArgumentException("growthMax must be >= growthMin");
        }
        this.checkIntervalTicks = checkIntervalTicks;
        this.growthChance = growthChance;
        this.growthMin = growthMin;
        this.growthMax = growthMax;
        this.random = random;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public List<WorldEvent> decide(World world, long tick) {
        if (tick == 0 || tick % this.checkIntervalTicks != 0) {
            return List.of();
        }
        List<WorldEvent> events = new ArrayList<>();
        for (IslandStatus status : world.islandStatuses()) {
            if (this.random.nextDouble() >= this.growthChance) {
                continue;
            }
            int amount = this.growthMin + this.random.nextInt(this.growthMax - this.growthMin + 1);
            events.add(new IslandPopulationGrew(tick, status.islandId(), amount));
        }
        return List.copyOf(events);
    }
}
