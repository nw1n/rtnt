package com.example.rtnt.adapter.out.persistence.game;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameLoopStateMongoRepository extends MongoRepository<GameLoopStateDocument, String> {
}
