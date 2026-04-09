package com.example.rtnt.domain.ship;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ship entities.
 * This is a domain interface - implementations belong in the infrastructure layer.
 */
public interface ShipRepository {
    /**
     * Save a ship (create or update).
     * @param ship the ship to save
     * @return the saved ship
     */
    Ship save(Ship ship);

    /**
     * Find a ship by ID.
     * @param id the ship ID
     * @return Optional containing the ship if found
     */
    Optional<Ship> findById(String id);

    /**
     * Find all ships.
     * @return list of all ships
     */
    List<Ship> findAll();

    /**
     * Find ships anchored at a specific island.
     * @param islandId the island ID
     * @return list of ships anchored at the island
     */
    List<Ship> findByIslandId(String islandId);

    /**
     * Find ships controlled by a specific player.
     * @param playerId the player ID
     * @return list of ships controlled by the player
     */
    List<Ship> findByPlayerId(String playerId);

    /**
     * Delete a ship by ID.
     * @param id the ship ID
     */
    void deleteById(String id);

    /**
     * Delete all ships.
     */
    void deleteAll();

    /**
     * Number of ships persisted.
     */
    long count();

    /**
     * Check if a ship exists.
     * @param id the ship ID
     * @return true if ship exists
     */
    boolean existsById(String id);
}
