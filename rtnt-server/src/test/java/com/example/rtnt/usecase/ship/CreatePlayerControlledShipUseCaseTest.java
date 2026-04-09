package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.player.Player;
import com.example.rtnt.domain.player.PlayerRepository;
import com.example.rtnt.domain.player.StandardPlayer;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import com.example.rtnt.domain.ship.StandardShip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePlayerControlledShipUseCaseTest {

    @Mock
    private ShipFactory shipFactory;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ShipRepository shipRepository;

    @InjectMocks
    private CreatePlayerControlledShipUseCase useCase;

    @Test
    void createSavesFactoryShip() {
        Player createdPlayer = StandardPlayer.fromDocument("player-1", "Jack Sparrow", "#112233");
        Ship generated = StandardShip.fromDocument("ship-1", "Poseidon", null, null, "player-1", Inventory.empty());
        when(playerRepository.save(org.mockito.ArgumentMatchers.any(Player.class))).thenReturn(createdPlayer);
        when(shipFactory.createWithNameForPlayer("Poseidon", "player-1")).thenReturn(generated);
        when(shipRepository.save(generated)).thenReturn(generated);

        CreatePlayerControlledShipUseCase.CreationResult created = useCase.create("Poseidon", "Jack Sparrow");

        assertEquals("player-1", created.player().getId());
        assertEquals("Jack Sparrow", created.player().getName());
        assertEquals("#112233", created.player().getHexColor());
        assertEquals("ship-1", created.ship().getId());
        assertEquals("player-1", created.ship().getPlayerId());
        verify(playerRepository, times(1)).save(org.mockito.ArgumentMatchers.any(Player.class));
        verify(shipFactory, times(1)).createWithNameForPlayer("Poseidon", "player-1");
        verify(shipRepository, times(1)).save(generated);
    }

    @Test
    void createRejectsBlankShipName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(" ", "Jack Sparrow"));
        assertEquals("Ship name is required", ex.getMessage());
    }

    @Test
    void createRejectsBlankPlayerName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create("Poseidon", " "));
        assertEquals("Player name is required", ex.getMessage());
    }
}
