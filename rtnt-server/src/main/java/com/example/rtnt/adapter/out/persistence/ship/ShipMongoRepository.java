package com.example.rtnt.adapter.out.persistence.ship;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for Ship documents.
 */
@Repository
public interface ShipMongoRepository extends MongoRepository<ShipDocument, String> {
    List<ShipDocument> findByIslandId(String islandId);
    List<ShipDocument> findByPlayerId(String playerId);
}
