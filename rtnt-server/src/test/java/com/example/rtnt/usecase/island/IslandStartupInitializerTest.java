package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IslandStartupInitializerTest {

    @Mock
    private IslandRepository islandRepository;

    @Mock
    private IslandFactory islandFactory;

    private IslandStartupInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new IslandStartupInitializer(islandRepository, islandFactory, 15);
    }

    @Test
    void runSeedsWhenStoreEmpty() {
        when(islandRepository.count()).thenReturn(0L);
        when(islandFactory.createRandomizedWithCaribbeanName(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> {
                    int placedCount = ((java.util.List<?>) invocation.getArgument(0)).size();
                    Footprint footprint = Footprint.create(placedCount * 100, placedCount * 100, 50, 50);
                    return StandardIsland.create("TestName", footprint, Inventory.empty(), TradePriceList.defaultPrices());
                });

        initializer.run();

        org.mockito.InOrder inOrder = inOrder(islandRepository);
        inOrder.verify(islandRepository, times(1)).count();
        inOrder.verify(islandRepository, times(15)).save(org.mockito.ArgumentMatchers.any(Island.class));
        verify(islandRepository, never()).deleteAll();
        verify(islandFactory, times(15)).createRandomizedWithCaribbeanName(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void runSkipsSeedWhenStoreNotEmpty() {
        when(islandRepository.count()).thenReturn(3L);

        initializer.run();

        verify(islandRepository, times(1)).count();
        verify(islandRepository, never()).deleteAll();
        verify(islandRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(islandFactory, never()).createRandomizedWithCaribbeanName(org.mockito.ArgumentMatchers.anyList());
    }
}
