package com.example.rtnt.game.core.event;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public record World(List<Island> islands, List<IslandStatus> islandStatuses) {
    public World {
        islands = List.copyOf(islands);
        islandStatuses = List.copyOf(islandStatuses);
    }

    public static World empty() {
        return new World(List.of(), List.of());
    }

    public static World of(List<Island> islands, List<IslandStatus> islandStatuses) {
        return new World(islands, islandStatuses);
    }

    public World apply(WorldEvent event) {
        return switch (event) {
            case WorldCleared ignored -> empty();
            case IslandCreated created -> this.applyCreated(created);
            case IslandPopulationGrew grew -> this.applyGrew(grew);
        };
    }

    public World applyAll(Iterable<WorldEvent> events) {
        World world = this;
        for (WorldEvent event : events) {
            world = world.apply(event);
        }
        return world;
    }

    private World applyCreated(IslandCreated created) {
        List<Island> nextIslands = new ArrayList<>(this.islands);
        nextIslands.removeIf(island -> island.id().equals(created.islandId()));
        nextIslands.add(created.toIsland());
        List<IslandStatus> nextStatuses = new ArrayList<>(this.islandStatuses);
        nextStatuses.removeIf(status -> status.islandId().equals(created.islandId()));
        nextStatuses.add(IslandStatus.initial(created.islandId()));
        return new World(nextIslands, nextStatuses);
    }

    private World applyGrew(IslandPopulationGrew grew) {
        List<IslandStatus> nextStatuses = new ArrayList<>();
        boolean found = false;
        for (IslandStatus status : this.islandStatuses) {
            if (status.islandId().equals(grew.islandId())) {
                nextStatuses.add(status.grow(grew.amount()));
                found = true;
            } else {
                nextStatuses.add(status);
            }
        }
        if (!found) {
            nextStatuses.add(IslandStatus.initial(grew.islandId()).grow(grew.amount()));
        }
        return new World(this.islands, nextStatuses);
    }
}
