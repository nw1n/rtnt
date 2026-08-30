package com.example.rtnt.game.core.flow.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameFlowStatusMongoRepository extends MongoRepository<GameFlowStatusDocument, String> {
}
