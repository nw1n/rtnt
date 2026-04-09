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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IslandOverstockConsumeServiceTest {

    @Mock
    private IslandRepository islandRepository;

    @Test
    void amountAfterConsume_leavesAtOrBelowThresholdUnchanged() {
        assertEquals(0, IslandOverstockConsumeService.amountAfterConsume(0, 100));
        assertEquals(100, IslandOverstockConsumeService.amountAfterConsume(100, 100));
    }

    @Test
    void amountAfterConsume_halvesWhenStrictlyOverThreshold() {
        assertEquals(50, IslandOverstockConsumeService.amountAfterConsume(101, 100));
        assertEquals(100, IslandOverstockConsumeService.amountAfterConsume(200, 100));
        assertEquals(250, IslandOverstockConsumeService.amountAfterConsume(501, 100));
    }

    @Test
    void consumeSurplusTick_halvesOverstockAndSaves() {
        Island island = StandardIsland.fromDocument(
                "i1",
                "Tortuga",
                Footprint.create(0, 0, 50, 50),
                Inventory.of(Map.of(
                        GoodType.GOLD, 500,
                        GoodType.RUM, 100,
                        GoodType.SUGAR, 400,
                        GoodType.SPICES, 2,
                        GoodType.TOBACCO, 301
                )),
                TradePriceList.defaultPrices()
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandOverstockConsumeService service = new IslandOverstockConsumeService(islandRepository, 100);
        service.consumeSurplusTick();

        assertEquals(500, island.getInventory().getAmount(GoodType.GOLD));
        assertEquals(100, island.getInventory().getAmount(GoodType.RUM));
        assertEquals(200, island.getInventory().getAmount(GoodType.SUGAR));
        assertEquals(2, island.getInventory().getAmount(GoodType.SPICES));
        assertEquals(150, island.getInventory().getAmount(GoodType.TOBACCO));
        verify(islandRepository, times(1)).save(island);
    }

    @Test
    void consumeSurplusTick_doesNotSaveWhenNothingOverThreshold() {
        Island island = StandardIsland.fromDocument(
                "i2",
                "Empty",
                Footprint.create(0, 0, 40, 40),
                Inventory.of(Map.of(
                        GoodType.GOLD, 50,
                        GoodType.RUM, 100
                )),
                TradePriceList.defaultPrices()
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandOverstockConsumeService service = new IslandOverstockConsumeService(islandRepository, 100);
        service.consumeSurplusTick();

        assertEquals(50, island.getInventory().getAmount(GoodType.GOLD));
        assertEquals(100, island.getInventory().getAmount(GoodType.RUM));
        verify(islandRepository, never()).save(island);
    }

    @Test
    void applyConsumeToIsland_returnsWhetherInventoryChanged() {
        Island over = StandardIsland.fromDocument(
                "o",
                "O",
                Footprint.create(0, 0, 10, 10),
                Inventory.of(Map.of(GoodType.RUM, 350)),
                TradePriceList.defaultPrices()
        );
        Island ok = StandardIsland.fromDocument(
                "k",
                "K",
                Footprint.create(1, 1, 10, 10),
                Inventory.of(Map.of(GoodType.RUM, 10)),
                TradePriceList.defaultPrices()
        );
        IslandOverstockConsumeService service = new IslandOverstockConsumeService(islandRepository, 100);

        assertTrue(service.applyConsumeToIsland(over));
        assertFalse(service.applyConsumeToIsland(ok));
    }
}
