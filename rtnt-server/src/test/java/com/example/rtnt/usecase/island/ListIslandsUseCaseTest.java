package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListIslandsUseCaseTest {

    @Mock
    private IslandRepository islandRepository;

    @InjectMocks
    private ListIslandsUseCase listIslandsUseCase;

    @Test
    void getAllIslandsDelegatesToRepository() {
        // Given
        List<Island> expected = Collections.emptyList();
        when(islandRepository.findAll()).thenReturn(expected);

        // When
        List<Island> actual = listIslandsUseCase.getAllIslands();

        // Then
        assertSame(expected, actual);
        verify(islandRepository, times(1)).findAll();
    }
}
