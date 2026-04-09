package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.player.Player;
import com.example.rtnt.domain.player.PlayerRepository;
import com.example.rtnt.domain.player.StandardPlayer;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import org.springframework.stereotype.Component;

@Component
public class CreatePlayerControlledShipUseCase {

    private final ShipFactory shipFactory;
    private final PlayerRepository playerRepository;
    private final ShipRepository shipRepository;

    public CreatePlayerControlledShipUseCase(
            ShipFactory shipFactory,
            PlayerRepository playerRepository,
            ShipRepository shipRepository
    ) {
        this.shipFactory = shipFactory;
        this.playerRepository = playerRepository;
        this.shipRepository = shipRepository;
    }

    public CreationResult create(String shipName, String playerName) {
        if (shipName == null || shipName.isBlank()) {
            throw new IllegalArgumentException("Ship name is required");
        }
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name is required");
        }

        Player player = this.playerRepository.save(StandardPlayer.create(playerName.trim()));
        Ship ship = this.shipFactory.createWithNameForPlayer(shipName.trim(), player.getId());
        Ship savedShip = this.shipRepository.save(ship);
        return new CreationResult(player, savedShip);
    }

    public CreationResult createForExistingPlayer(String shipName, String playerId) {
        if (shipName == null || shipName.isBlank()) {
            throw new IllegalArgumentException("Ship name is required");
        }
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player ID is required");
        }

        Player player = this.playerRepository.findById(playerId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Ship ship = this.shipFactory.createWithNameForPlayer(shipName.trim(), player.getId());
        Ship savedShip = this.shipRepository.save(ship);
        return new CreationResult(player, savedShip);
    }

    public record CreationResult(Player player, Ship ship) {
    }
}
