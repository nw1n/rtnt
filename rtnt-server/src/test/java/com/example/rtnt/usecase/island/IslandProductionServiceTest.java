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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IslandProductionServiceTest {

    @Mock
    private IslandRepository islandRepository;

    @Test
    void produceGoodsTickWithCertainBoom_addsBoomAmountToOneTradeableGood() {
        Island island = StandardIsland.fromDocument(
                "island-a",
                "A",
                Footprint.create(0, 0, 60, 60),
                Inventory.of(Map.of(
                        GoodType.GOLD, 100,
                        GoodType.RUM, 1,
                        GoodType.SUGAR, 2,
                        GoodType.SPICES, 3,
                        GoodType.TOBACCO, 4
                )),
                TradePriceList.defaultPrices()
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandProductionService service = new IslandProductionService(islandRepository, 1.0D, 30);
        int beforeTradeables = island.getInventory().sumTradeableGoods();

        service.produceGoodsTick();

        assertEquals(beforeTradeables + 30, island.getInventory().sumTradeableGoods());
        verify(islandRepository, times(1)).save(island);
    }

    @Test
    void produceGoodsTickWithNoBoom_doesNotSave() {
        Island island = StandardIsland.fromDocument(
                "island-b",
                "B",
                Footprint.create(10, 10, 60, 60),
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandProductionService service = new IslandProductionService(islandRepository, 0.0D, 30);
        int before = island.getInventory().sumTradeableGoods();

        service.produceGoodsTick();

        assertEquals(before, island.getInventory().sumTradeableGoods());
        verify(islandRepository, never()).save(island);
    }

    @Test
    void rollBoom_probabilityZeroNever() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < 50; i++) {
            assertFalse(IslandProductionService.rollBoom(rnd, 0.0D));
        }
    }

    @Test
    void rollBoom_probabilityOneAlways() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < 50; i++) {
            assertTrue(IslandProductionService.rollBoom(rnd, 1.0D));
        }
    }

    @Test
    void produceGoodsTick_respectsCustomBoomAmount() {
        Island island = StandardIsland.fromDocument(
                "island-c",
                "C",
                Footprint.create(0, 0, 40, 40),
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );
        when(islandRepository.findAll()).thenReturn(List.of(island));

        IslandProductionService service = new IslandProductionService(islandRepository, 1.0D, 37);
        service.produceGoodsTick();

        ArgumentCaptor<Island> captor = ArgumentCaptor.forClass(Island.class);
        verify(islandRepository, times(1)).save(captor.capture());
        assertEquals(37, captor.getValue().getInventory().sumTradeableGoods());
    }
}
