package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.location.Footprint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class IslandFactoryTest {

    @Mock
    private IslandNameProvider islandNameProvider;

    @InjectMocks
    private IslandFactory islandFactory;

    @Test
    void createWithCaribbeanNameUsesNextProviderName() {
        // Given
        Footprint footprint = Footprint.create(10, 20, 60, 60);
        when(islandNameProvider.getNextName()).thenReturn("Jamaica");

        // When
        Island created = islandFactory.createWithCaribbeanName(footprint);

        // Then
        assertEquals("Jamaica", created.getName());
        assertEquals(10, created.getFootprint().getX());
        assertEquals(20, created.getFootprint().getY());
        assertEquals(60, created.getFootprint().getWidth());
        assertEquals(60, created.getFootprint().getLength());
        assertNotNull(created.getId());
        assertEquals(3, created.getTradePrices().getPrice(GoodType.RUM));
        assertEquals(2, created.getTradePrices().getPrice(GoodType.SUGAR));
        assertEquals(4, created.getTradePrices().getPrice(GoodType.SPICES));
        assertEquals(5, created.getTradePrices().getPrice(GoodType.TOBACCO));
        verify(islandNameProvider, times(1)).getNextName();
        verify(islandNameProvider, never()).getRandomName();
    }

    @Test
    void createWithRandomCaribbeanNameUsesRandomProviderName() {
        // Given
        Footprint footprint = Footprint.create(1, 2, 60, 60);
        when(islandNameProvider.getRandomName()).thenReturn("Cuba");

        // When
        Island created = islandFactory.createWithRandomCaribbeanName(footprint);

        // Then
        assertEquals("Cuba", created.getName());
        assertEquals(1, created.getFootprint().getX());
        assertEquals(2, created.getFootprint().getY());
        assertEquals(60, created.getFootprint().getWidth());
        assertEquals(60, created.getFootprint().getLength());
        assertNotNull(created.getId());
        assertEquals(3, created.getTradePrices().getPrice(GoodType.RUM));
        verify(islandNameProvider, times(1)).getRandomName();
        verify(islandNameProvider, never()).getNextName();
    }

    @Test
    void createWithNameBypassesProviderAndGeneratesIds() {
        // Given
        Footprint footprint = Footprint.create(5, 6, 60, 60);

        // When
        Island createdA = islandFactory.createWithName("Trinidad", footprint);
        Island createdB = islandFactory.createWithName("Trinidad", footprint);

        // Then
        assertEquals("Trinidad", createdA.getName());
        assertEquals(5, createdA.getFootprint().getX());
        assertEquals(6, createdA.getFootprint().getY());
        assertEquals(60, createdA.getFootprint().getWidth());
        assertEquals(60, createdA.getFootprint().getLength());
        assertNotNull(createdA.getId());
        assertNotNull(createdB.getId());
        assertNotEquals(createdA.getId(), createdB.getId());
        verify(islandNameProvider, never()).getNextName();
        verify(islandNameProvider, never()).getRandomName();
    }

    @Test
    void createRandomizedWithNameCreatesIslandWithinConfiguredBounds() {
        Island created = islandFactory.createRandomizedWithName("Nassau", List.of());

        assertEquals("Nassau", created.getName());
        assertTrue(created.getFootprint().getX() >= 0);
        assertTrue(created.getFootprint().getY() >= 0);
        assertTrue(created.getFootprint().getX() + created.getFootprint().getWidth() <= 2000);
        assertTrue(created.getFootprint().getY() + created.getFootprint().getLength() <= 1000);
        assertTrue(created.getFootprint().getWidth() >= 20 && created.getFootprint().getWidth() <= 100);
        assertTrue(created.getFootprint().getLength() >= 20 && created.getFootprint().getLength() <= 100);
    }

    @Test
    void createRandomizedWithCaribbeanNameUsesNextNameProvider() {
        when(islandNameProvider.getNextName()).thenReturn("Antigua");

        Island created = islandFactory.createRandomizedWithCaribbeanName(List.of());

        assertEquals("Antigua", created.getName());
        verify(islandNameProvider, times(1)).getNextName();
    }
}
