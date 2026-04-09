package com.example.rtnt.adapter.out.persistence.player;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerMongoRepository extends MongoRepository<PlayerDocument, String> {
}
