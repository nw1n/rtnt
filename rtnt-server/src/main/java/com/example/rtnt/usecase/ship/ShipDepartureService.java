package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.JourneyRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import com.example.rtnt.domain.ship.StandardShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ShipDepartureService {

    private static final Logger log = LoggerFactory.getLogger(ShipDepartureService.class);
    private static final long DEFAULT_DEPARTURE_INTERVAL_MS = 10_000L;

    private final ShipRepository shipRepository;
    private final IslandRepository islandRepository;
    private final JourneyRepository journeyRepository;
    private final NpcShipTradingService npcShipTradingService;

    public ShipDepartureService(
            ShipRepository shipRepository,
            IslandRepository islandRepository,
            JourneyRepository journeyRepository,
            NpcShipTradingService npcShipTradingService
    ) {
        this.shipRepository = shipRepository;
        this.islandRepository = islandRepository;
        this.journeyRepository = journeyRepository;
        this.npcShipTradingService = npcShipTradingService;
    }

    @Scheduled(
            fixedRateString = "${rtnt.ship-departures.interval-ms:" + DEFAULT_DEPARTURE_INTERVAL_MS + "}",
            initialDelayString = "${rtnt.ship-departures.initial-delay-ms:0}"
    )
    public void scheduledCheckDepartures() {
        this.checkDepartures();
        this.arrivalCheck();
    }

    public void checkDepartures() {
        List<Island> islands = this.islandRepository.findAll();
        if (islands.size() < 2) {
            return;
        }

        Map<String, Island> islandsById = islands.stream()
                .collect(Collectors.toMap(Island::getId, Function.identity()));

        for (Ship ship : this.shipRepository.findAll()) {
            if (isPlayerControlled(ship)) {
                continue;
            }

            String currentIslandId = ship.getIslandId();
            if (currentIslandId == null || currentIslandId.isBlank()) {
                continue;
            }

            Island currentIsland = islandsById.get(currentIslandId);
            if (currentIsland == null) {
                continue;
            }

            List<Island> destinations = islands.stream()
                    .filter(island -> !island.getId().equals(currentIslandId))
                    .toList();
            if (destinations.isEmpty()) {
                continue;
            }

            this.npcShipTradingService.tryNpcBuyOnce(ship.getId());
            Ship shipAfterBuy = this.shipRepository.findById(ship.getId()).orElse(ship);

            Island destination = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
            Instant departed = Instant.now();
            Duration travelDuration = calculateTravelDuration(currentIsland, destination, shipAfterBuy.getSpeed());
            Instant estimatedArrival = departed.plus(travelDuration);
            Journey journey = new Journey(
                    UUID.randomUUID().toString(),
                    shipAfterBuy.getId(),
                    currentIslandId,
                    destination.getId(),
                    departed,
                    null,
                    estimatedArrival,
                    true
            );
            this.journeyRepository.save(journey);

            Ship departedShip = StandardShip.fromDocument(
                    shipAfterBuy.getId(),
                    shipAfterBuy.getName(),
                    null,
                    journey,
                    shipAfterBuy.getPlayerId(),
                    shipAfterBuy.getInventory()
            );

            this.shipRepository.save(departedShip);
            log.info(
                    "Ship {} departed {} for {} (travel ~{}s, journey {})",
                    shipAfterBuy.getName(),
                    currentIsland.getName(),
                    destination.getName(),
                    travelDuration.getSeconds(),
                    journey.id()
            );
        }
    }

    public void arrivalCheck() {
        Instant now = Instant.now();
        Map<String, Island> islandsById = this.islandRepository.findAll().stream()
                .collect(Collectors.toMap(Island::getId, Function.identity()));
        for (Ship ship : this.shipRepository.findAll()) {
            Journey journey = ship.getJourney();
            if (journey == null) {
                continue;
            }
            if (ship.getIslandId() != null && !ship.getIslandId().isBlank()) {
                continue;
            }
            if (journey.estimatedArrival() == null || journey.estimatedArrival().isAfter(now)) {
                continue;
            }

            Journey arrivedJourney = new Journey(
                    journey.id(),
                    journey.shipId(),
                    journey.startIslandId(),
                    journey.targetIslandId(),
                    journey.departed(),
                    now,
                    journey.estimatedArrival(),
                    false
            );
            this.journeyRepository.save(arrivedJourney);

            Ship arrivedShip = StandardShip.fromDocument(
                    ship.getId(),
                    ship.getName(),
                    journey.targetIslandId(),
                    arrivedJourney,
                    ship.getPlayerId(),
                    ship.getInventory()
            );
            this.shipRepository.save(arrivedShip);
            String arrivedAt = islandsById.containsKey(journey.targetIslandId())
                    ? islandsById.get(journey.targetIslandId()).getName()
                    : journey.targetIslandId();
            log.info(
                    "Ship {} arrived at {} (journey {}, started from {})",
                    ship.getName(),
                    arrivedAt,
                    journey.id(),
                    islandsById.containsKey(journey.startIslandId())
                            ? islandsById.get(journey.startIslandId()).getName()
                            : journey.startIslandId()
            );
            this.npcShipTradingService.tryNpcSellOnce(ship.getId());
        }
    }

    private static boolean isPlayerControlled(Ship ship) {
        return ship.getPlayerId() != null && !ship.getPlayerId().isBlank();
    }

    private static Duration calculateTravelDuration(Island from, Island to, int speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Ship speed must be greater than 0");
        }
        double distance = calculateDistance(from.getFootprint(), to.getFootprint());
        long travelSeconds = Math.max(1L, (long) Math.ceil(distance / speed));
        return Duration.ofSeconds(travelSeconds);
    }

    private static double calculateDistance(Footprint a, Footprint b) {
        double ax = a.getX() + (a.getWidth() / 2.0);
        double ay = a.getY() + (a.getLength() / 2.0);
        double bx = b.getX() + (b.getWidth() / 2.0);
        double by = b.getY() + (b.getLength() / 2.0);
        return Math.hypot(ax - bx, ay - by);
    }
}
