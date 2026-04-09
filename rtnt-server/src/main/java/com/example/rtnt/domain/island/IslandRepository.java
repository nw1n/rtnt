package com.example.rtnt.domain.island;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Island entities.
 * This is a domain interface - implementations belong in the infrastructure layer.
 */
public interface IslandRepository {
    /**
     * Save an island (create or update).
     * @param island the island to save
     * @return the saved island
     */
    Island save(Island island);

    /**
     * Find an island by ID.
     * @param id the island ID
     * @return Optional containing the island if found
     */
    Optional<Island> findById(String id);

    /**
     * Find all islands.
     * @return list of all islands
     */
    List<Island> findAll();

    /**
     * Delete an island by ID.
     * @param id the island ID
     */
    void deleteById(String id);

    /**
     * Delete all islands.
     */
    void deleteAll();

    /**
     * Number of islands persisted.
     */
    long count();

    /**
     * Check if an island exists.
     * @param id the island ID
     * @return true if island exists
     */
    boolean existsById(String id);
}
