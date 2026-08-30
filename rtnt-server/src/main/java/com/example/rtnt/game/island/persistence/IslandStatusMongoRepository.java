package com.example.rtnt.game.island.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IslandStatusMongoRepository extends MongoRepository<IslandStatusDocument, String> {
}
