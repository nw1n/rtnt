package com.example.rtnt.game.ship.web;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Journey;
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
                .map(ship -> ShipDto.from(ship, islandNameById))
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
            int cargoCapacity,
            @Nullable JourneyDto journey
    ) {
        static ShipDto from(Ship ship, Map<String, String> islandNameById) {
            String islandId = ship.getIslandId();
            return new ShipDto(
                    ship.getId(),
                    ship.getName(),
                    islandId,
                    islandId == null ? null : islandNameById.get(islandId),
                    ship.getPlayerId(),
                    ship.getSpeed(),
                    ship.getCargoCapacity(),
                    JourneyDto.from(ship.getJourney(), islandNameById)
            );
        }
    }

    public record JourneyDto(
            String id,
            String startIslandId,
            @Nullable String startIslandName,
            String targetIslandId,
            @Nullable String targetIslandName,
            long departedTick,
            @Nullable Long arrivedTick,
            long estimatedArrivalTick,
            boolean active
    ) {
        static @Nullable JourneyDto from(@Nullable Journey journey, Map<String, String> islandNameById) {
            if (journey == null) {
                return null;
            }
            return new JourneyDto(
                    journey.id(),
                    journey.startIslandId(),
                    islandNameById.get(journey.startIslandId()),
                    journey.targetIslandId(),
                    islandNameById.get(journey.targetIslandId()),
                    journey.departedTick(),
                    journey.arrivedTick(),
                    journey.estimatedArrivalTick(),
                    journey.active()
            );
        }
    }
}
