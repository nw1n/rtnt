package com.example.rtnt.game.log.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameLogMongoRepository extends MongoRepository<GameLogDocument, String> {
    List<GameLogDocument> findTop200ByOrderByTickDesc();
}
