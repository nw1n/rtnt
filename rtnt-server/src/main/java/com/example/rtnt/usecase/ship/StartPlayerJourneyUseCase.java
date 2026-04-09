package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.JourneyRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import com.example.rtnt.domain.ship.StandardShip;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class StartPlayerJourneyUseCase {

    private final ShipRepository shipRepository;
    private final IslandRepository islandRepository;
    private final JourneyRepository journeyRepository;

    public StartPlayerJourneyUseCase(
            ShipRepository shipRepository,
            IslandRepository islandRepository,
            JourneyRepository journeyRepository
    ) {
        this.shipRepository = shipRepository;
        this.islandRepository = islandRepository;
        this.journeyRepository = journeyRepository;
    }

    public Ship startJourney(String shipId, String playerId, String targetIslandId) {
        if (shipId == null || shipId.isBlank()) {
            throw new IllegalArgumentException("Ship ID is required");
        }
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player ID is required");
        }
        if (targetIslandId == null || targetIslandId.isBlank()) {
            throw new IllegalArgumentException("Target island ID is required");
        }

        Ship ship = this.shipRepository.findById(shipId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Ship not found"));

        if (ship.getPlayerId() == null || !ship.getPlayerId().equals(playerId.trim())) {
            throw new IllegalArgumentException("Player does not control this ship");
        }
        if (ship.getIslandId() == null || ship.getIslandId().isBlank()) {
            throw new IllegalStateException("Ship is currently at sea");
        }
        if (ship.getIslandId().equals(targetIslandId.trim())) {
            throw new IllegalArgumentException("Ship is already at the target island");
        }

        Island startIsland = this.islandRepository.findById(ship.getIslandId())
                .orElseThrow(() -> new IllegalArgumentException("Start island not found"));
        Island targetIsland = this.islandRepository.findById(targetIslandId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Target island not found"));

        Instant departed = Instant.now();
        Duration travelDuration = calculateTravelDuration(startIsland.getFootprint(), targetIsland.getFootprint(), ship.getSpeed());
        Instant estimatedArrival = departed.plus(travelDuration);
        Journey journey = new Journey(
                UUID.randomUUID().toString(),
                ship.getId(),
                startIsland.getId(),
                targetIsland.getId(),
                departed,
                null,
                estimatedArrival,
                true
        );
        this.journeyRepository.save(journey);

        Ship departedShip = StandardShip.fromDocument(
                ship.getId(),
                ship.getName(),
                null,
                journey,
                ship.getPlayerId(),
                ship.getInventory()
        );
        return this.shipRepository.save(departedShip);
    }

    private static Duration calculateTravelDuration(Footprint from, Footprint to, int speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Ship speed must be greater than 0");
        }
        double distance = calculateDistance(from, to);
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
