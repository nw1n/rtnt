package com.example.rtnt.adapter.out.persistence.journey;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JourneyMongoRepository extends MongoRepository<JourneyDocument, String> {
    List<JourneyDocument> findTop100ByOrderByDepartedDesc();
    List<JourneyDocument> findByShipId(String shipId);
}
