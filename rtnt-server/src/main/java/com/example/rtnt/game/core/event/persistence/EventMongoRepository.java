package com.example.rtnt.game.core.event.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventMongoRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findByStreamIdOrderByStreamVersionAsc(String streamId);

    Optional<EventDocument> findTopByStreamIdOrderByStreamVersionDesc(String streamId);
}
