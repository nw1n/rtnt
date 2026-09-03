package com.example.rtnt.game.ship.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipMongoRepository extends MongoRepository<ShipDocument, String> {
}
