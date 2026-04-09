package com.example.rtnt.adapter.out.persistence.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.StandardShip;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB document representation of a Ship.
 * This is the persistence model that maps to the domain model.
 */
@Document(collection = "ships")
public class ShipDocument {
    @Id
    private String id;
    private String name;
    private String islandId;
    private JourneyDocument journey;
    private String playerId;
    private Map<String, Integer> inventory;

    // Default constructor for Spring Data MongoDB
    public ShipDocument() {
    }

    public ShipDocument(String id, String name) {
        this(id, name, null, null, null, null);
    }

    public ShipDocument(String id, String name, String islandId) {
        this(id, name, islandId, null, null, null);
    }

    public ShipDocument(String id, String name, String islandId, Map<String, Integer> inventory) {
        this(id, name, islandId, null, null, inventory);
    }

    public ShipDocument(
            String id,
            String name,
            String islandId,
            JourneyDocument journey,
            String playerId,
            Map<String, Integer> inventory
    ) {
        this.id = id;
        this.name = name;
        this.islandId = islandId;
        this.journey = journey;
        this.playerId = playerId;
        this.inventory = inventory;
    }

    /**
     * Convert from domain model to persistence model.
     */
    public static ShipDocument fromDomain(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship cannot be null");
        }
        return new ShipDocument(
                ship.getId(),
                ship.getName(),
                ship.getIslandId(),
                JourneyDocument.fromDomain(ship.getJourney()),
                ship.getPlayerId(),
                toStoredInventory(ship.getInventory())
        );
    }

    /**
     * Convert from persistence model to domain model.
     */
    public Ship toDomain() {
        return StandardShip.fromDocument(
                this.id,
                this.name,
                this.islandId,
                this.journey == null ? null : this.journey.toDomain(),
                this.playerId,
                fromStoredInventory(this.inventory)
        );
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIslandId() {
        return this.islandId;
    }

    public void setIslandId(String islandId) {
        this.islandId = islandId;
    }

    public JourneyDocument getJourney() {
        return this.journey;
    }

    public void setJourney(JourneyDocument journey) {
        this.journey = journey;
    }

    public static class JourneyDocument {
        private String id;
        private String shipId;
        private String startIslandId;
        private String targetIslandId;
        private java.time.Instant departed;
        private java.time.Instant arrived;
        private java.time.Instant estimatedArrival;
        private Boolean active;

        public JourneyDocument() {
        }

        public JourneyDocument(
                String id,
                String shipId,
                String startIslandId,
                String targetIslandId,
                java.time.Instant departed,
                java.time.Instant arrived,
                java.time.Instant estimatedArrival,
                Boolean active
        ) {
            this.id = id;
            this.shipId = shipId;
            this.startIslandId = startIslandId;
            this.targetIslandId = targetIslandId;
            this.departed = departed;
            this.arrived = arrived;
            this.estimatedArrival = estimatedArrival;
            this.active = active;
        }

        static JourneyDocument fromDomain(Journey journey) {
            if (journey == null) {
                return null;
            }
            return new JourneyDocument(
                    journey.id(),
                    journey.shipId(),
                    journey.startIslandId(),
                    journey.targetIslandId(),
                    journey.departed(),
                    journey.arrived(),
                    journey.estimatedArrival(),
                    journey.active()
            );
        }

        Journey toDomain() {
            return new Journey(
                    this.id,
                    this.shipId,
                    this.startIslandId,
                    this.targetIslandId,
                    this.departed,
                    this.arrived,
                    this.estimatedArrival,
                    Journey.resolveActiveFromStorage(this.active)
            );
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getShipId() {
            return this.shipId;
        }

        public void setShipId(String shipId) {
            this.shipId = shipId;
        }

        public String getStartIslandId() {
            return this.startIslandId;
        }

        public void setStartIslandId(String startIslandId) {
            this.startIslandId = startIslandId;
        }

        public String getTargetIslandId() {
            return this.targetIslandId;
        }

        public void setTargetIslandId(String targetIslandId) {
            this.targetIslandId = targetIslandId;
        }

        public java.time.Instant getDeparted() {
            return this.departed;
        }

        public void setDeparted(java.time.Instant departed) {
            this.departed = departed;
        }

        public java.time.Instant getArrived() {
            return this.arrived;
        }

        public void setArrived(java.time.Instant arrived) {
            this.arrived = arrived;
        }

        public java.time.Instant getEstimatedArrival() {
            return this.estimatedArrival;
        }

        public void setEstimatedArrival(java.time.Instant estimatedArrival) {
            this.estimatedArrival = estimatedArrival;
        }

        public Boolean getActive() {
            return this.active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Map<String, Integer> getInventory() {
        return this.inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }

    private static Map<String, Integer> toStoredInventory(Inventory inventory) {
        Map<String, Integer> stored = new HashMap<>();
        Inventory safeInventory = inventory == null ? Inventory.empty() : inventory;
        for (GoodType goodType : GoodType.values()) {
            stored.put(goodType.name(), safeInventory.getAmount(goodType));
        }
        return stored;
    }

    private static Inventory fromStoredInventory(Map<String, Integer> inventory) {
        if (inventory == null) {
            return Inventory.empty();
        }

        EnumMap<GoodType, Integer> amounts = new EnumMap<>(GoodType.class);
        for (GoodType goodType : GoodType.values()) {
            Integer amount = inventory.get(goodType.name());
            amounts.put(goodType, amount == null ? 0 : amount);
        }
        return Inventory.of(amounts);
    }
}
