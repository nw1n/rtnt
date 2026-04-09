package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.domain.ship.Ship;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipFactoryTest {

    @Mock
    private ShipNameProvider shipNameProvider;

    @Mock
    private IslandRepository islandRepository;

    @InjectMocks
    private ShipFactory shipFactory;

    @Test
    void createWithCaribbeanNameUsesNextProviderName() {
        // Given
        when(shipNameProvider.getNextName()).thenReturn("Black Pearl");
        when(islandRepository.findAll()).thenReturn(List.of(sampleIsland("island-1")));

        // When
        Ship created = shipFactory.createWithCaribbeanName();

        // Then
        assertEquals("Black Pearl", created.getName());
        assertNotNull(created.getId());
        assertNotNull(created.getIslandId());
        verify(shipNameProvider, times(1)).getNextName();
        verify(shipNameProvider, never()).getRandomName();
    }

    @Test
    void createWithRandomCaribbeanNameUsesRandomProviderName() {
        // Given
        when(shipNameProvider.getRandomName()).thenReturn("Flying Dutchman");
        when(islandRepository.findAll()).thenReturn(List.of(sampleIsland("island-1")));

        // When
        Ship created = shipFactory.createWithRandomCaribbeanName();

        // Then
        assertEquals("Flying Dutchman", created.getName());
        assertNotNull(created.getId());
        assertNotNull(created.getIslandId());
        verify(shipNameProvider, times(1)).getRandomName();
        verify(shipNameProvider, never()).getNextName();
    }

    @Test
    void createWithNameBypassesProviderAndGeneratesIds() {
        // When
        when(islandRepository.findAll()).thenReturn(List.of(sampleIsland("island-1")));
        Ship createdA = shipFactory.createWithName("Interceptor");
        Ship createdB = shipFactory.createWithName("Interceptor");

        // Then
        assertEquals("Interceptor", createdA.getName());
        assertNotNull(createdA.getId());
        assertNotNull(createdB.getId());
        assertNotNull(createdA.getIslandId());
        assertNotNull(createdB.getIslandId());
        assertNotEquals(createdA.getId(), createdB.getId());
        verify(shipNameProvider, never()).getNextName();
        verify(shipNameProvider, never()).getRandomName();
    }

    @Test
    void createWithNameForPlayerAssignsController() {
        when(islandRepository.findAll()).thenReturn(List.of(sampleIsland("island-1")));
        Ship created = shipFactory.createWithNameForPlayer("Interceptor", "player-12");

        assertEquals("Interceptor", created.getName());
        assertEquals("player-12", created.getPlayerId());
        assertNotNull(created.getIslandId());
        assertNotNull(created.getId());
        verify(shipNameProvider, never()).getNextName();
        verify(shipNameProvider, never()).getRandomName();
    }

    private static Island sampleIsland(String id) {
        return StandardIsland.fromDocument(
                id,
                "Sample",
                Footprint.create(0, 0, 50, 50),
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );
    }
}
