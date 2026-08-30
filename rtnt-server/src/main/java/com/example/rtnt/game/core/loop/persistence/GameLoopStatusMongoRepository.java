package com.example.rtnt.game.loop.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameLoopStatusMongoRepository extends MongoRepository<GameLoopStatusDocument, String> {
}
