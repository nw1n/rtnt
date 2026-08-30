package com.example.rtnt.game.core.worldsnapshot.web;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/world-snapshots")
public class WorldSnapshotController {
    private final WorldSnapshotStore worldSnapshotStore;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public WorldSnapshotController(WorldSnapshotStore worldSnapshotStore) {
        this.worldSnapshotStore = worldSnapshotStore;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public List<WorldSnapshotDto> getAll() {
        return this.worldSnapshotStore.list().stream()
                .map(WorldSnapshotDto::from)
                .toList();
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record WorldSnapshotDto(long tick, List<IslandPopulationDto> islands) {
        static WorldSnapshotDto from(WorldSnapshot snapshot) {
            Map<String, Long> populationByIslandId = snapshot.islandStatuses().stream()
                    .collect(Collectors.toMap(IslandStatus::islandId, IslandStatus::population, (left, right) -> left));
            return new WorldSnapshotDto(
                    snapshot.tick(),
                    snapshot.islands().stream()
                            .map(island -> IslandPopulationDto.from(island, populationByIslandId.getOrDefault(island.id(), 0L)))
                            .toList()
            );
        }
    }

    public record IslandPopulationDto(String id, String name, long population) {
        static IslandPopulationDto from(Island island, long population) {
            return new IslandPopulationDto(island.id(), island.name(), population);
        }
    }
}
