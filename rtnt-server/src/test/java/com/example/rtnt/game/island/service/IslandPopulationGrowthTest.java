package com.example.rtnt.game.island.service;

import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.persistence.IslandStatusDocument;
import com.example.rtnt.game.island.persistence.IslandStatusMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IslandPopulationGrowthTest {

    @Mock
    private IslandStatusMongoRepository islandStatusMongoRepository;

    @Test
    void skipsTicksOffInterval() {
        IslandPopulationGrowth growth = this.growth(1.0, new Random(1));

        assertFalse(growth.applyIfDue(0));
        assertFalse(growth.applyIfDue(9));
        verify(this.islandStatusMongoRepository, never()).findAll();
    }

    @Test
    void growsWhenChanceIsCertain() {
        when(this.islandStatusMongoRepository.findAll())
                .thenReturn(List.of(IslandStatusDocument.from(IslandStatus.initial("a"))));
        IslandPopulationGrowth growth = this.growth(1.0, new Random(1));

        assertTrue(growth.applyIfDue(10));

        ArgumentCaptor<List<IslandStatusDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.islandStatusMongoRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals("a", captor.getValue().getFirst().islandId());
        assertTrue(captor.getValue().getFirst().population() >= 1);
    }

    @Test
    void doesNotGrowWhenChanceIsZero() {
        when(this.islandStatusMongoRepository.findAll())
                .thenReturn(List.of(IslandStatusDocument.from(IslandStatus.initial("a"))));
        IslandPopulationGrowth growth = this.growth(0.0, new Random(1));

        assertFalse(growth.applyIfDue(10));
        verify(this.islandStatusMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    private IslandPopulationGrowth growth(double chance, Random random) {
        return new IslandPopulationGrowth(
                this.islandStatusMongoRepository,
                10,
                chance,
                1,
                3,
                random
        );
    }
}
