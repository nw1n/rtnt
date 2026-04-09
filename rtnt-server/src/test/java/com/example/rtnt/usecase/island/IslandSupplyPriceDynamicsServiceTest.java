package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IslandSupplyPriceDynamicsServiceTest {

    @Mock
    private IslandRepository islandRepository;

    @Test
    void newPriceForSupply_highStockDecreasesPrice() {
        assertEquals(
                9,
                IslandSupplyPriceDynamicsService.newPriceForSupply(10, 81, 20, 80, 1, 1, 50)
        );
    }

    @Test
    void newPriceForSupply_lowStockIncreasesPrice() {
        assertEquals(
                11,
                IslandSupplyPriceDynamicsService.newPriceForSupply(10, 19, 20, 80, 1, 1, 50)
        );
    }

    @Test
    void newPriceForSupply_inBandUnchanged() {
        assertEquals(
                10,
                IslandSupplyPriceDynamicsService.newPriceForSupply(10, 50, 20, 80, 1, 1, 50)
        );
        assertEquals(
                10,
                IslandSupplyPriceDynamicsService.newPriceForSupply(10, 20, 20, 80, 1, 1, 50)
        );
        assertEquals(
                10,
                IslandSupplyPriceDynamicsService.newPriceForSupply(10, 80, 20, 80, 1, 1, 50)
        );
    }

    @Test
    void newPriceForSupply_clampsToMinAndMax() {
        assertEquals(1, IslandSupplyPriceDynamicsService.newPriceForSupply(1, 100, 20, 80, 2, 1, 50));
        assertEquals(50, IslandSupplyPriceDynamicsService.newPriceForSupply(50, 0, 20, 80, 5, 1, 50));
    }

    @Test
    void newPriceForSupply_rejectsInvalidConfig() {
        assertThrows(
                IllegalArgumentException.class,
                () -> IslandSupplyPriceDynamicsService.newPriceForSupply(5, 50, 80, 20, 1, 1, 50)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> IslandSupplyPriceDynamicsService.newPriceForSupply(5, 50, 20, 80, 0, 1, 50)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> IslandSupplyPriceDynamicsService.newPriceForSupply(5, 50, 20, 80, 1, 0, 50)
        );
    }

    @Test
    void constructorRejectsInvalidTargetBand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new IslandSupplyPriceDynamicsService(islandRepository, 90, 20, 1, 1, 50)
        );
    }

    @Test
    void applySupplyPricingToIsland_onlyAdjustsGoodsOutsideBand() {
        Island island = StandardIsland.fromDocument(
                "i1",
                "A",
                Footprint.create(0, 0, 50, 50),
                Inventory.of(Map.of(
                        GoodType.GOLD, 100,
                        GoodType.RUM, 100,
                        GoodType.SUGAR, 50,
                        GoodType.SPICES, 50,
                        GoodType.TOBACCO, 50
                )),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 25,
                        GoodType.SUGAR, 10,
                        GoodType.SPICES, 10,
                        GoodType.TOBACCO, 10
                ))
        );
        IslandSupplyPriceDynamicsService service = new IslandSupplyPriceDynamicsService(
                islandRepository, 20, 80, 1, 1, 50
        );

        assertTrue(service.applySupplyPricingToIsland(island));

        assertEquals(24, island.getTradePrices().getPrice(GoodType.RUM));
        assertEquals(10, island.getTradePrices().getPrice(GoodType.SUGAR));
        assertEquals(10, island.getTradePrices().getPrice(GoodType.SPICES));
        assertEquals(10, island.getTradePrices().getPrice(GoodType.TOBACCO));
    }

    @Test
    void adjustPricesTick_savesWhenPricesChanged() {
        Island island = StandardIsland.fromDocument(
                "i2",
                "B",
                Footprint.create(0, 0, 40, 40),
                Inventory.of(Map.of(GoodType.RUM, 0)),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 5,
                        GoodType.SUGAR, 5,
                        GoodType.SPICES, 5,
                        GoodType.TOBACCO, 5
                ))
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandSupplyPriceDynamicsService service = new IslandSupplyPriceDynamicsService(
                islandRepository, 20, 80, 1, 1, 50
        );
        service.adjustPricesTick();

        assertEquals(6, island.getTradePrices().getPrice(GoodType.RUM));
        assertEquals(6, island.getTradePrices().getPrice(GoodType.SUGAR));
        verify(islandRepository, times(1)).save(island);
    }

    @Test
    void adjustPricesTick_skipsSaveWhenNothingChanges() {
        Island island = StandardIsland.fromDocument(
                "i3",
                "C",
                Footprint.create(0, 0, 30, 30),
                Inventory.of(Map.of(
                        GoodType.RUM, 50,
                        GoodType.SUGAR, 50,
                        GoodType.SPICES, 50,
                        GoodType.TOBACCO, 50
                )),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 10,
                        GoodType.SUGAR, 10,
                        GoodType.SPICES, 10,
                        GoodType.TOBACCO, 10
                ))
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandSupplyPriceDynamicsService service = new IslandSupplyPriceDynamicsService(
                islandRepository, 20, 80, 1, 1, 50
        );
        service.adjustPricesTick();

        verify(islandRepository, never()).save(island);
    }
}
