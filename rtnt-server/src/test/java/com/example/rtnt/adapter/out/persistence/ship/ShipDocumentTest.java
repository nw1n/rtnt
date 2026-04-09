package com.example.rtnt.adapter.out.persistence.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.StandardShip;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipDocumentTest {

    @Test
    void testFromDomain() {
        // Given
        Ship ship = StandardShip.fromDocument(
                "ship-id-123",
                "Black Pearl",
                "island-1",
                new Journey("journey-1", "ship-id-123", "island-1", "island-2", Instant.parse("2026-03-21T10:00:00Z"), Instant.parse("2026-03-21T12:00:00Z"), Instant.parse("2026-03-21T11:30:00Z"), false),
                "player-1",
                Inventory.of(Map.of(
                        GoodType.GOLD, 12,
                        GoodType.RUM, 3,
                        GoodType.SUGAR, 2,
                        GoodType.SPICES, 4,
                        GoodType.TOBACCO, 5
                ))
        );

        // When
        ShipDocument document = ShipDocument.fromDomain(ship);

        // Then
        assertNotNull(document);
        assertEquals("ship-id-123", document.getId());
        assertEquals("Black Pearl", document.getName());
        assertEquals("island-1", document.getIslandId());
        assertEquals("journey-1", document.getJourney().getId());
        assertEquals("player-1", document.getPlayerId());
        assertNotNull(document.getInventory());
        assertEquals(12, document.getInventory().get("GOLD"));
    }

    @Test
    void testFromDomainWithNullShip() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ShipDocument.fromDomain(null);
        });
        assertEquals("Ship cannot be null", exception.getMessage());
    }

    @Test
    void testToDomain() {
        // Given
        ShipDocument document = new ShipDocument(
                "ship-id-456",
                "Interceptor",
                "island-9",
                new ShipDocument.JourneyDocument("journey-9", "ship-id-456", "island-9", "island-10", Instant.parse("2026-03-21T08:00:00Z"), null, Instant.parse("2026-03-21T13:00:00Z"), true),
                "player-9",
                Map.of(
                        "GOLD", 33,
                        "RUM", 8,
                        "SUGAR", 7,
                        "SPICES", 6,
                        "TOBACCO", 9
                )
        );

        // When
        Ship ship = document.toDomain();

        // Then
        assertNotNull(ship);
        assertEquals("ship-id-456", ship.getId());
        assertEquals("Interceptor", ship.getName());
        assertEquals("island-9", ship.getIslandId());
        assertEquals("journey-9", ship.getJourney().id());
        assertEquals(Instant.parse("2026-03-21T08:00:00Z"), ship.getJourney().departed());
        assertEquals(null, ship.getJourney().arrived());
        assertEquals(Instant.parse("2026-03-21T13:00:00Z"), ship.getJourney().estimatedArrival());
        assertEquals(true, ship.getJourney().active());
        assertEquals("player-9", ship.getPlayerId());
        assertEquals(33, ship.getInventory().getAmount(GoodType.GOLD));
        assertEquals(8, ship.getInventory().getAmount(GoodType.RUM));
    }

    @Test
    void testRoundTripConversion() {
        // Given
        Ship original = StandardShip.fromDocument(
                "round-trip-id",
                "Queen Anne's Revenge",
                "island-3",
                new Journey("journey-rt", "round-trip-id", "island-3", "island-4", Instant.parse("2026-03-20T08:00:00Z"), Instant.parse("2026-03-20T14:00:00Z"), Instant.parse("2026-03-20T13:30:00Z"), false),
                "player-3",
                Inventory.of(Map.of(
                        GoodType.GOLD, 70,
                        GoodType.RUM, 10,
                        GoodType.SUGAR, 8,
                        GoodType.SPICES, 6,
                        GoodType.TOBACCO, 4
                ))
        );

        // When
        ShipDocument document = ShipDocument.fromDomain(original);
        Ship converted = document.toDomain();

        // Then
        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getIslandId(), converted.getIslandId());
        assertEquals(original.getJourney(), converted.getJourney());
        assertEquals(original.getPlayerId(), converted.getPlayerId());
        assertEquals(original.getInventory(), converted.getInventory());
    }

    @Test
    void testShipDocumentGettersAndSetters() {
        // Given
        ShipDocument document = new ShipDocument();

        // When
        document.setId("setter-id");
        document.setName("Setter Ship");
        document.setIslandId("setter-island");
        document.setJourney(new ShipDocument.JourneyDocument("journey-setter", "setter-id", "setter-island", "setter-target", Instant.parse("2026-03-21T09:00:00Z"), Instant.parse("2026-03-21T10:00:00Z"), Instant.parse("2026-03-21T09:50:00Z"), false));
        document.setPlayerId("setter-player");
        document.setInventory(Map.of("GOLD", 1));

        // Then
        assertEquals("setter-id", document.getId());
        assertEquals("Setter Ship", document.getName());
        assertEquals("setter-island", document.getIslandId());
        assertEquals("journey-setter", document.getJourney().getId());
        assertEquals("setter-player", document.getPlayerId());
        assertEquals(1, document.getInventory().get("GOLD"));
    }
}
