package com.example.rtnt.domain.player;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Player entities.
 */
public interface PlayerRepository {
    Player save(Player player);

    Optional<Player> findById(String id);

    List<Player> findAll();

    void deleteById(String id);

    void deleteAll();

    boolean existsById(String id);
}
