package com.example.rtnt.game.log.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameLogMongoRepository extends MongoRepository<GameLogDocument, String> {
}
