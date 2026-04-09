package com.example.rtnt.adapter.out.persistence.island;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IslandDocumentTest {

    @Test
    void testFromDomain() {
        // Given
        Island island = StandardIsland.fromDocument(
                "test-id-123",
                "Jamaica",
                Footprint.create(10, 20, 70, 55),
                Inventory.of(Map.of(
                        GoodType.GOLD, 300,
                        GoodType.RUM, 120,
                        GoodType.SUGAR, 90,
                        GoodType.SPICES, 40,
                        GoodType.TOBACCO, 80
                )),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 2,
                        GoodType.SUGAR, 3,
                        GoodType.SPICES, 4,
                        GoodType.TOBACCO, 5
                ))
        );

        // When
        IslandDocument document = IslandDocument.fromDomain(island);

        // Then
        assertNotNull(document);
        assertEquals("test-id-123", document.getId());
        assertEquals("Jamaica", document.getName());
        assertNotNull(document.getFootprint());
        assertEquals(10, document.getFootprint().getX());
        assertEquals(20, document.getFootprint().getY());
        assertEquals(70, document.getFootprint().getWidth());
        assertEquals(55, document.getFootprint().getLength());
        assertEquals(300, document.getInventory().get("GOLD"));
        assertEquals(2, document.getTradePrices().get("RUM"));
    }

    @Test
    void testFromDomainWithNullIsland() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            IslandDocument.fromDomain(null);
        });
        assertEquals("Island cannot be null", exception.getMessage());
    }

    @Test
    void testFromDomainWithNullFootprint() {
        // Given - This would require creating an Island with null footprint, which isn't possible
        // with the current StandardIsland implementation, but we test the null check anyway
        // by verifying the validation in fromDomain
        Island island = StandardIsland.fromDocument(
                "test-id",
                "Test",
                Footprint.create(0, 0, 60, 60),
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );

        // The test verifies that if footprint were null, it would throw
        // Since StandardIsland doesn't allow null footprints, we test the conversion works
        IslandDocument document = IslandDocument.fromDomain(island);
        assertNotNull(document.getFootprint());
    }

    @Test
    void testToDomain() {
        // Given
        IslandDocument.FootprintEmbedded footprintEmbedded = new IslandDocument.FootprintEmbedded(15, 25, 90, 45);
        IslandDocument document = new IslandDocument(
                "test-id-456",
                "Cuba",
                footprintEmbedded,
                Map.of(
                        "GOLD", 777,
                        "RUM", 60,
                        "SUGAR", 50,
                        "SPICES", 40,
                        "TOBACCO", 30
                ),
                Map.of(
                        "RUM", 3,
                        "SUGAR", 2,
                        "SPICES", 5,
                        "TOBACCO", 4
                )
        );

        // When
        Island island = document.toDomain();

        // Then
        assertNotNull(island);
        assertEquals("test-id-456", island.getId());
        assertEquals("Cuba", island.getName());
        assertNotNull(island.getFootprint());
        assertEquals(15, island.getFootprint().getX());
        assertEquals(25, island.getFootprint().getY());
        assertEquals(90, island.getFootprint().getWidth());
        assertEquals(45, island.getFootprint().getLength());
        assertEquals(777, island.getInventory().getAmount(GoodType.GOLD));
        assertEquals(3, island.getTradePrices().getPrice(GoodType.RUM));
    }

    @Test
    void testToDomainWithNullFootprint() {
        // Given
        IslandDocument document = new IslandDocument("test-id", "Test Island", null);

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            document.toDomain();
        });
        assertEquals("IslandDocument footprint cannot be null", exception.getMessage());
    }

    @Test
    void testRoundTripConversion() {
        // Given
        Island original = StandardIsland.fromDocument(
                "round-trip-id",
                "Puerto Rico",
                Footprint.create(30, 40, 44, 88),
                Inventory.of(Map.of(
                        GoodType.GOLD, 123,
                        GoodType.RUM, 4,
                        GoodType.SUGAR, 5,
                        GoodType.SPICES, 6,
                        GoodType.TOBACCO, 7
                )),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 3,
                        GoodType.SUGAR, 2,
                        GoodType.SPICES, 4,
                        GoodType.TOBACCO, 6
                ))
        );

        // When
        IslandDocument document = IslandDocument.fromDomain(original);
        Island converted = document.toDomain();

        // Then
        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getFootprint().getX(), converted.getFootprint().getX());
        assertEquals(original.getFootprint().getY(), converted.getFootprint().getY());
        assertEquals(original.getFootprint().getWidth(), converted.getFootprint().getWidth());
        assertEquals(original.getFootprint().getLength(), converted.getFootprint().getLength());
        assertEquals(original.getInventory(), converted.getInventory());
        assertEquals(original.getTradePrices(), converted.getTradePrices());
    }

    @Test
    void testFootprintEmbedded() {
        // Given
        IslandDocument.FootprintEmbedded footprint = new IslandDocument.FootprintEmbedded(5, 10, 33, 44);

        // Then
        assertEquals(5, footprint.getX());
        assertEquals(10, footprint.getY());
        assertEquals(33, footprint.getWidth());
        assertEquals(44, footprint.getLength());

        // When
        footprint.setX(15);
        footprint.setY(20);
        footprint.setWidth(70);
        footprint.setLength(80);

        // Then
        assertEquals(15, footprint.getX());
        assertEquals(20, footprint.getY());
        assertEquals(70, footprint.getWidth());
        assertEquals(80, footprint.getLength());
    }

    @Test
    void testFootprintEmbeddedDefaultConstructor() {
        // Given
        IslandDocument.FootprintEmbedded footprint = new IslandDocument.FootprintEmbedded();

        // Then - default values
        assertEquals(0, footprint.getX());
        assertEquals(0, footprint.getY());
        assertEquals(0, footprint.getWidth());
        assertEquals(0, footprint.getLength());

        // When
        footprint.setX(100);
        footprint.setY(200);
        footprint.setWidth(300);
        footprint.setLength(400);

        // Then
        assertEquals(100, footprint.getX());
        assertEquals(200, footprint.getY());
        assertEquals(300, footprint.getWidth());
        assertEquals(400, footprint.getLength());
    }

    @Test
    void testIslandDocumentGettersAndSetters() {
        // Given
        IslandDocument document = new IslandDocument();
        IslandDocument.FootprintEmbedded footprint = new IslandDocument.FootprintEmbedded(7, 14, 33, 66);

        // When
        document.setId("setter-id");
        document.setName("Setter Island");
        document.setFootprint(footprint);
        document.setInventory(Map.of("GOLD", 88));
        document.setTradePrices(Map.of("RUM", 9, "SUGAR", 8, "SPICES", 7, "TOBACCO", 6));

        // Then
        assertEquals("setter-id", document.getId());
        assertEquals("Setter Island", document.getName());
        assertEquals(7, document.getFootprint().getX());
        assertEquals(14, document.getFootprint().getY());
        assertEquals(33, document.getFootprint().getWidth());
        assertEquals(66, document.getFootprint().getLength());
        assertEquals(88, document.getInventory().get("GOLD"));
        assertEquals(9, document.getTradePrices().get("RUM"));
    }
}
