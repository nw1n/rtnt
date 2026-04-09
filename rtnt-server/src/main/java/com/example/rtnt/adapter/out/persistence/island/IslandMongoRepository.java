package com.example.rtnt.adapter.out.persistence.island;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for Island documents.
 */
@Repository
public interface IslandMongoRepository extends MongoRepository<IslandDocument, String> {
}
