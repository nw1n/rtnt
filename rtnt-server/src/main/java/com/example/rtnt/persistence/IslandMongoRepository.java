package com.example.rtnt.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IslandMongoRepository extends MongoRepository<IslandDocument, String> {
}
