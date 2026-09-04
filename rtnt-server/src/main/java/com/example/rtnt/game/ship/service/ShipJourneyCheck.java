package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Journey;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.domain.ShipTravel;
import com.example.rtnt.game.ship.persistence.ShipDocument;
import com.example.rtnt.game.ship.persistence.ShipMongoRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@NullMarked
public class ShipJourneyCheck {
    private static final Logger log = LoggerFactory.getLogger(ShipJourneyCheck.class);

    private final ShipMongoRepository shipMongoRepository;
    private final IslandService islandService;
    private final int checkIntervalTicks;
    private final Random random;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    @Autowired
    public ShipJourneyCheck(
            ShipMongoRepository shipMongoRepository,
            IslandService islandService,
            @Value("${rtnt.ship.journey-check-interval-ticks:5}") int checkIntervalTicks
    ) {
        this(shipMongoRepository, islandService, checkIntervalTicks, new Random());
    }

    ShipJourneyCheck(
            ShipMongoRepository shipMongoRepository,
            IslandService islandService,
            int checkIntervalTicks,
            Random random
    ) {
        if (checkIntervalTicks < 1) {
            throw new IllegalArgumentException("checkIntervalTicks must be at least 1");
        }
        this.shipMongoRepository = shipMongoRepository;
        this.islandService = islandService;
        this.checkIntervalTicks = checkIntervalTicks;
        this.random = random;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public boolean applyIfDue(long tick) {
        if (tick == 0 || tick % this.checkIntervalTicks != 0) {
            return false;
        }
        List<Island> islands = this.islandService.list();
        if (islands.size() < 2) {
            return false;
        }
        Map<String, Island> islandsById = islands.stream()
                .collect(Collectors.toMap(Island::id, Function.identity(), (left, right) -> left));
        List<ShipDocument> updated = new ArrayList<>();
        for (ShipDocument document : this.shipMongoRepository.findAll()) {
            Ship changed = this.apply(document.toShip(), islands, islandsById, tick);
            if (changed != null) {
                updated.add(ShipDocument.from(changed));
            }
        }
        if (updated.isEmpty()) {
            return false;
        }
        this.shipMongoRepository.saveAll(updated);
        return true;
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private @Nullable Ship apply(
            Ship ship,
            List<Island> islands,
            Map<String, Island> islandsById,
            long tick
    ) {
        Journey journey = ship.getJourney();
        if (journey == null || !journey.active()) {
            return this.departIdleShip(ship, islands, islandsById, tick);
        }
        if (journey.estimatedArrivalTick() > tick) {
            return null;
        }
        Ship arrived = ship.withIslandAndJourney(journey.targetIslandId(), journey.complete(tick));
        log.info(
                "Ship {} arrived at {} (journey {})",
                arrived.getName(),
                journey.targetIslandId(),
                journey.id()
        );
        return arrived;
    }

    private @Nullable Ship departIdleShip(
            Ship ship,
            List<Island> islands,
            Map<String, Island> islandsById,
            long tick
    ) {
        String playerId = ship.getPlayerId();
        if (playerId != null && !playerId.isBlank()) {
            return null;
        }
        String currentIslandId = ship.getIslandId();
        if (currentIslandId == null || currentIslandId.isBlank()) {
            return null;
        }
        Island currentIsland = islandsById.get(currentIslandId);
        if (currentIsland == null) {
            return null;
        }
        List<Island> destinations = islands.stream()
                .filter(island -> !island.id().equals(currentIslandId))
                .toList();
        if (destinations.isEmpty()) {
            return null;
        }
        Island destination = destinations.get(this.random.nextInt(destinations.size()));
        long estimatedArrivalTick = tick + ShipTravel.ticks(
                currentIsland.footprint(),
                destination.footprint(),
                ship.getSpeed()
        );
        Journey journey = Journey.create(currentIslandId, destination.id(), tick, estimatedArrivalTick);
        Ship departed = ship.withIslandAndJourney(null, journey);
        log.info(
                "Ship {} departed {} for {} (eta tick {})",
                departed.getName(),
                currentIsland.name(),
                destination.name(),
                estimatedArrivalTick
        );
        return departed;
    }
}
