package com.example.rtnt.game.ship.persistence;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.ship.domain.Journey;
import com.example.rtnt.game.ship.domain.Ship;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "ships")
@NullMarked
public record ShipDocument(
        @Id String id,
        String name,
        @Nullable String islandId,
        @Nullable JourneyDocument journey,
        @Nullable String playerId,
        Map<String, Integer> inventory
) {
    /***************************************************************************
     *                                                                         *
     * Nested types                                                            *
     *                                                                         *
     **************************************************************************/

    public record JourneyDocument(
            String id,
            String shipId,
            String startIslandId,
            String targetIslandId,
            Instant departed,
            @Nullable Instant arrived,
            Instant estimatedArrival,
            @Nullable Boolean active
    ) {
        static JourneyDocument from(Journey journey) {
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

        Journey toJourney() {
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
    }

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static ShipDocument from(Ship ship) {
        Journey currentJourney = ship.getJourney();
        return new ShipDocument(
                ship.getId(),
                ship.getName(),
                ship.getIslandId(),
                currentJourney == null ? null : JourneyDocument.from(currentJourney),
                ship.getPlayerId(),
                toStoredInventory(ship.getInventory())
        );
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Ship toShip() {
        JourneyDocument currentJourney = this.journey;
        return Ship.existing(
                this.id,
                this.name,
                this.islandId,
                currentJourney == null ? null : currentJourney.toJourney(),
                this.playerId,
                fromStoredInventory(this.inventory)
        );
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private static Map<String, Integer> toStoredInventory(Inventory inventory) {
        Map<String, Integer> stored = new HashMap<>();
        for (GoodType goodType : GoodType.values()) {
            stored.put(goodType.name(), inventory.getAmount(goodType));
        }
        return stored;
    }

    private static Inventory fromStoredInventory(@Nullable Map<String, Integer> inventory) {
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
