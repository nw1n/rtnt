package com.example.rtnt.service;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.persistence.island.IslandMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IslandServiceTest {

    @Mock
    private IslandMongoRepository islandMongoRepository;

    @Mock
    private IslandNames islandNames;

    @Test
    void createRandomizedPlacesIslandWithinConfiguredBounds() {
        IslandService islandService = new IslandService(this.islandMongoRepository, this.islandNames, 15);

        Island created = islandService.createRandomized("Nassau", List.of());

        assertEquals("Nassau", created.getName());
        assertTrue(created.getFootprint().getX() >= 0);
        assertTrue(created.getFootprint().getY() >= 0);
        assertTrue(created.getFootprint().getX() + created.getFootprint().getWidth() <= 2000);
        assertTrue(created.getFootprint().getY() + created.getFootprint().getLength() <= 1000);
        assertTrue(created.getFootprint().getWidth() >= 20 && created.getFootprint().getWidth() <= 100);
        assertTrue(created.getFootprint().getLength() >= 20 && created.getFootprint().getLength() <= 100);
        assertEquals(3, created.getTradePrices().getPrice(GoodType.RUM));
        assertEquals(100, created.getInventory().getAmount(GoodType.GOLD));
    }
}
