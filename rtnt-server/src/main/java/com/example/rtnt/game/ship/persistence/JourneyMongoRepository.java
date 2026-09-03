package com.example.rtnt.game.ship.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyMongoRepository extends MongoRepository<JourneyDocument, String> {
}
