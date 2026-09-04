package com.example.rtnt.game.island.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.domain.TradePriceList;
import com.example.rtnt.game.island.persistence.IslandStatusDocument;
import com.example.rtnt.game.island.persistence.IslandStatusMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IslandEconomyTest {

    @Mock
    private IslandStatusMongoRepository islandStatusMongoRepository;

    @Test
    void skipsTicksOffInterval() {
        IslandEconomy economy = this.economy(1.0, 20);

        assertFalse(economy.applyIfDue(0));
        assertFalse(economy.applyIfDue(9));
        verify(this.islandStatusMongoRepository, never()).findAll();
    }

    @Test
    void foodAppearsThreeTimesAsOftenInProductionBag() {
        List<GoodType> bag = IslandEconomy.productionBag();
        long food = bag.stream().filter(good -> good == GoodType.FOOD).count();
        for (GoodType goodType : GoodType.tradeableGoods()) {
            if (goodType == GoodType.FOOD) {
                continue;
            }
            long other = bag.stream().filter(good -> good == goodType).count();
            assertEquals(1, other);
            assertEquals(3 * other, food);
        }
    }

    @Test
    void producesTradeableGoodWhenChanceIsCertain() {
        IslandStatus empty = new IslandStatus("a", 0, Inventory.empty(), TradePriceList.defaultPrices());
        when(this.islandStatusMongoRepository.findAll()).thenReturn(List.of(IslandStatusDocument.from(empty)));
        IslandEconomy economy = this.economy(1.0, 20);

        assertTrue(economy.applyIfDue(10));

        IslandStatus saved = this.savedStatus();
        assertEquals(0, saved.population());
        assertTrue(saved.inventory().sumTradeableGoods() >= 1);
        assertEquals(0, saved.inventory().getAmount(GoodType.GOLD));
    }

    @Test
    void doesNotProduceWhenChanceIsZero() {
        IslandStatus empty = new IslandStatus("a", 0, Inventory.empty(), TradePriceList.defaultPrices());
        when(this.islandStatusMongoRepository.findAll()).thenReturn(List.of(IslandStatusDocument.from(empty)));
        IslandEconomy economy = this.economy(0.0, 20);

        assertFalse(economy.applyIfDue(10));
        verify(this.islandStatusMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void growsAndConsumesHalfWhenAllGoodsMeetThreshold() {
        IslandStatus abundant = new IslandStatus(
                "a",
                4,
                Inventory.of(Map.of(
                        GoodType.GOLD, 100,
                        GoodType.FOOD, 20,
                        GoodType.RUM, 21,
                        GoodType.SUGAR, 20,
                        GoodType.SPICES, 22,
                        GoodType.TOBACCO, 23
                )),
                TradePriceList.defaultPrices()
        );
        when(this.islandStatusMongoRepository.findAll()).thenReturn(List.of(IslandStatusDocument.from(abundant)));
        IslandEconomy economy = this.economy(0.0, 20);

        assertTrue(economy.applyIfDue(10));

        IslandStatus saved = this.savedStatus();
        assertTrue(saved.population() >= 5);
        assertEquals(100, saved.inventory().getAmount(GoodType.GOLD));
        assertEquals(10, saved.inventory().getAmount(GoodType.FOOD));
        assertEquals(10, saved.inventory().getAmount(GoodType.RUM));
        assertEquals(10, saved.inventory().getAmount(GoodType.SUGAR));
        assertEquals(11, saved.inventory().getAmount(GoodType.SPICES));
        assertEquals(11, saved.inventory().getAmount(GoodType.TOBACCO));
    }

    @Test
    void doesNotGrowWhenAGoodIsBelowThreshold() {
        IslandStatus shortOnRum = new IslandStatus(
                "a",
                4,
                Inventory.of(Map.of(
                        GoodType.FOOD, 20,
                        GoodType.RUM, 19,
                        GoodType.SUGAR, 20,
                        GoodType.SPICES, 20,
                        GoodType.TOBACCO, 20
                )),
                TradePriceList.defaultPrices()
        );
        when(this.islandStatusMongoRepository.findAll()).thenReturn(List.of(IslandStatusDocument.from(shortOnRum)));
        IslandEconomy economy = this.economy(0.0, 20);

        assertFalse(economy.applyIfDue(10));
        verify(this.islandStatusMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void consumesOneFoodOnFoodInterval() {
        IslandStatus fed = new IslandStatus(
                "a",
                10,
                Inventory.of(Map.of(GoodType.FOOD, 3)),
                TradePriceList.defaultPrices()
        );
        when(this.islandStatusMongoRepository.findAll()).thenReturn(List.of(IslandStatusDocument.from(fed)));
        IslandEconomy economy = this.foodEconomy();

        assertTrue(economy.applyIfDue(20));

        IslandStatus saved = this.savedStatus();
        assertEquals(10, saved.population());
        assertEquals(2, saved.inventory().getAmount(GoodType.FOOD));
    }

    @Test
    void starvesTenPercentRoundedUpWhenOutOfFood() {
        IslandStatus hungry = new IslandStatus(
                "a",
                11,
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );
        when(this.islandStatusMongoRepository.findAll()).thenReturn(List.of(IslandStatusDocument.from(hungry)));
        IslandEconomy economy = this.foodEconomy();

        assertTrue(economy.applyIfDue(20));

        IslandStatus saved = this.savedStatus();
        assertEquals(9, saved.population());
        assertEquals(0, saved.inventory().getAmount(GoodType.FOOD));
    }

    private IslandStatus savedStatus() {
        ArgumentCaptor<List<IslandStatusDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.islandStatusMongoRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        return captor.getValue().getFirst().toIslandStatus();
    }

    private IslandEconomy economy(double productionChance, int threshold) {
        return new IslandEconomy(
                this.islandStatusMongoRepository,
                10,
                1000,
                productionChance,
                1,
                3,
                threshold,
                1,
                3,
                new Random(1)
        );
    }

    private IslandEconomy foodEconomy() {
        return new IslandEconomy(
                this.islandStatusMongoRepository,
                1000,
                20,
                0.0,
                1,
                3,
                20,
                1,
                3,
                new Random(1)
        );
    }
}
