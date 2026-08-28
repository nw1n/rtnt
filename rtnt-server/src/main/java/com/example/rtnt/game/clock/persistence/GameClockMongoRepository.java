package com.example.rtnt.game.clock.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameClockMongoRepository extends MongoRepository<GameClockDocument, String> {
}
