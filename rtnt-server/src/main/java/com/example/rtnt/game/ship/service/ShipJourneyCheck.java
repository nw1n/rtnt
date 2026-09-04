package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Journey;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.domain.ShipTravel;
import com.example.rtnt.game.ship.persistence.ShipDocument;
import com.example.rtnt.game.ship.persistence.ShipMongoRepository;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeResult;
import com.example.rtnt.game.trade.service.ArrivalTrade;
import com.example.rtnt.game.trade.service.TradeEventStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@NullMarked
public class ShipJourneyCheck {
    private static final Logger log = LoggerFactory.getLogger(ShipJourneyCheck.class);

    private final ShipMongoRepository shipMongoRepository;
    private final IslandService islandService;
    private final ArrivalTrade arrivalTrade;
    private final TradeEventStore tradeEventStore;
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
            ArrivalTrade arrivalTrade,
            TradeEventStore tradeEventStore,
            @Value("${rtnt.ship.journey-check-interval-ticks:5}") int checkIntervalTicks
    ) {
        this(shipMongoRepository, islandService, arrivalTrade, tradeEventStore, checkIntervalTicks, new Random());
    }

    ShipJourneyCheck(
            ShipMongoRepository shipMongoRepository,
            IslandService islandService,
            ArrivalTrade arrivalTrade,
            TradeEventStore tradeEventStore,
            int checkIntervalTicks,
            Random random
    ) {
        if (checkIntervalTicks < 1) {
            throw new IllegalArgumentException("checkIntervalTicks must be at least 1");
        }
        this.shipMongoRepository = shipMongoRepository;
        this.islandService = islandService;
        this.arrivalTrade = arrivalTrade;
        this.tradeEventStore = tradeEventStore;
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
        Map<String, IslandStatus> statusesById = new HashMap<>();
        for (IslandStatus status : this.islandService.listStatuses()) {
            statusesById.put(status.islandId(), status);
        }
        List<ShipDocument> updatedShips = new ArrayList<>();
        Set<String> changedStatusIds = new HashSet<>();
        List<TradeEvent> events = new ArrayList<>();
        for (ShipDocument document : this.shipMongoRepository.findAll()) {
            ArrivalUpdate update = this.apply(document.toShip(), islands, islandsById, statusesById, tick);
            if (update == null) {
                continue;
            }
            updatedShips.add(ShipDocument.from(update.ship()));
            if (update.status() != null) {
                statusesById.put(update.status().islandId(), update.status());
                changedStatusIds.add(update.status().islandId());
            }
            events.addAll(update.events());
        }
        if (updatedShips.isEmpty()) {
            return false;
        }
        this.shipMongoRepository.saveAll(updatedShips);
        List<IslandStatus> statusesToSave = new ArrayList<>();
        for (String statusId : changedStatusIds) {
            IslandStatus status = statusesById.get(statusId);
            if (status != null) {
                statusesToSave.add(status);
            }
        }
        this.islandService.saveStatuses(statusesToSave);
        this.tradeEventStore.record(events);
        return true;
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private @Nullable ArrivalUpdate apply(
            Ship ship,
            List<Island> islands,
            Map<String, Island> islandsById,
            Map<String, IslandStatus> statusesById,
            long tick
    ) {
        Journey journey = ship.getJourney();
        if (journey == null || !journey.active()) {
            Ship departed = this.departIdleShip(ship, islands, islandsById, tick);
            return departed == null ? null : new ArrivalUpdate(departed, null, List.of());
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
        return this.tradeOnArrival(arrived, islandsById, statusesById, tick);
    }

    private ArrivalUpdate tradeOnArrival(
            Ship arrived,
            Map<String, Island> islandsById,
            Map<String, IslandStatus> statusesById,
            long tick
    ) {
        String playerId = arrived.getPlayerId();
        if (playerId != null && !playerId.isBlank()) {
            return new ArrivalUpdate(arrived, null, List.of());
        }
        String islandId = arrived.getIslandId();
        if (islandId == null) {
            return new ArrivalUpdate(arrived, null, List.of());
        }
        Island island = islandsById.get(islandId);
        IslandStatus status = statusesById.get(islandId);
        if (island == null || status == null) {
            return new ArrivalUpdate(arrived, null, List.of());
        }
        TradeResult result = this.arrivalTrade.execute(arrived, island, status, tick);
        return new ArrivalUpdate(result.ship(), result.islandStatus(), result.events());
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

    private record ArrivalUpdate(Ship ship, @Nullable IslandStatus status, List<TradeEvent> events) {
    }
}
