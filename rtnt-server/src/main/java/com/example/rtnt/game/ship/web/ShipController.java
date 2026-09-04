package com.example.rtnt.game.ship.web;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.service.ShipService;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ships")
public class ShipController {
    private final ShipService shipService;
    private final IslandService islandService;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ShipController(ShipService shipService, IslandService islandService) {
        this.shipService = shipService;
        this.islandService = islandService;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public List<ShipDto> getAll() {
        Map<String, String> islandNameById = this.islandService.list().stream()
                .collect(Collectors.toMap(Island::id, Island::name, (left, right) -> left));
        return this.shipService.list().stream()
                .map(ship -> ShipDto.from(ship, islandNameById.get(ship.getIslandId())))
                .toList();
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record ShipDto(
            String id,
            String name,
            @Nullable String islandId,
            @Nullable String islandName,
            @Nullable String playerId,
            int speed,
            int cargoCapacity
    ) {
        static ShipDto from(Ship ship, @Nullable String islandName) {
            return new ShipDto(
                    ship.getId(),
                    ship.getName(),
                    ship.getIslandId(),
                    islandName,
                    ship.getPlayerId(),
                    ship.getSpeed(),
                    ship.getCargoCapacity()
            );
        }
    }
}
