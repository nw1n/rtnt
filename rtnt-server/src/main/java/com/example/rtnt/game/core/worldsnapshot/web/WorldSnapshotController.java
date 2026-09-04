package com.example.rtnt.game.core.worldsnapshot.web;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.inventory.web.InventoryDto;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.web.TradePricesDto;
import com.example.rtnt.game.ship.domain.Ship;
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

    public record WorldSnapshotDto(long tick, List<IslandSnapshotDto> islands, List<ShipSnapshotDto> ships) {
        static WorldSnapshotDto from(WorldSnapshot snapshot) {
            Map<String, IslandStatus> statusByIslandId = snapshot.islandStatuses().stream()
                    .collect(Collectors.toMap(IslandStatus::islandId, status -> status, (left, right) -> left));
            return new WorldSnapshotDto(
                    snapshot.tick(),
                    snapshot.islands().stream()
                            .map(island -> IslandSnapshotDto.from(
                                    island,
                                    statusByIslandId.getOrDefault(
                                            island.id(),
                                            new IslandStatus(island.id(), 0, Inventory.empty())
                                    )
                            ))
                            .toList(),
                    snapshot.ships().stream().map(ShipSnapshotDto::from).toList()
            );
        }
    }

    public record IslandSnapshotDto(
            String id,
            String name,
            long population,
            InventoryDto inventory,
            TradePricesDto tradePrices
    ) {
        static IslandSnapshotDto from(Island island, IslandStatus status) {
            return new IslandSnapshotDto(
                    island.id(),
                    island.name(),
                    status.population(),
                    InventoryDto.from(status.inventory()),
                    TradePricesDto.from(island.tradePrices())
            );
        }
    }

    public record ShipSnapshotDto(String id, String name, InventoryDto inventory) {
        static ShipSnapshotDto from(Ship ship) {
            return new ShipSnapshotDto(ship.getId(), ship.getName(), InventoryDto.from(ship.getInventory()));
        }
    }
}
