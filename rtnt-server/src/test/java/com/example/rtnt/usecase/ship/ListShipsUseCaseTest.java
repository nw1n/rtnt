package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
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
class ListShipsUseCaseTest {

    @Mock
    private ShipRepository shipRepository;

    @InjectMocks
    private ListShipsUseCase listShipsUseCase;

    @Test
    void getAllShipsDelegatesToRepository() {
        // Given
        List<Ship> expected = Collections.emptyList();
        when(shipRepository.findAll()).thenReturn(expected);

        // When
        List<Ship> actual = listShipsUseCase.getAllShips();

        // Then
        assertSame(expected, actual);
        verify(shipRepository, times(1)).findAll();
    }

    @Test
    void getShipsAtIslandDelegatesToRepository() {
        // Given
        String islandId = "island-42";
        List<Ship> expected = Collections.emptyList();
        when(shipRepository.findByIslandId(islandId)).thenReturn(expected);

        // When
        List<Ship> actual = listShipsUseCase.getShipsAtIsland(islandId);

        // Then
        assertSame(expected, actual);
        verify(shipRepository, times(1)).findByIslandId(islandId);
    }

    @Test
    void getShipsForPlayerDelegatesToRepository() {
        String playerId = "player-42";
        List<Ship> expected = Collections.emptyList();
        when(shipRepository.findByPlayerId(playerId)).thenReturn(expected);

        List<Ship> actual = listShipsUseCase.getShipsForPlayer(playerId);

        assertSame(expected, actual);
        verify(shipRepository, times(1)).findByPlayerId(playerId);
    }
}
