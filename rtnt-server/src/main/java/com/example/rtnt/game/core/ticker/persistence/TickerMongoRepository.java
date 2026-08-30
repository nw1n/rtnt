package com.example.rtnt.game.core.ticker.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TickerMongoRepository extends MongoRepository<TickerDocument, String> {
}
