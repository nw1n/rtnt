package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Use-case service for retrieving all ships.
 */
@Component
public class ListShipsUseCase {

    private final ShipRepository shipRepository;

    public ListShipsUseCase(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public List<Ship> getAllShips() {
        return this.shipRepository.findAll();
    }

    public List<Ship> getShipsAtIsland(String islandId) {
        return this.shipRepository.findByIslandId(islandId);
    }

    public List<Ship> getShipsForPlayer(String playerId) {
        return this.shipRepository.findByPlayerId(playerId);
    }
}
