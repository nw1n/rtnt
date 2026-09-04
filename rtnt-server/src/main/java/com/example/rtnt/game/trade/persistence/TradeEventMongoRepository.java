package com.example.rtnt.game.trade.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeEventMongoRepository extends MongoRepository<TradeEventDocument, String> {
}
