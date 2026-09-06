package com.example.rtnt.game.core.event.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventMongoRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findByStreamIdOrderByStreamVersionAsc(String streamId);

    List<EventDocument> findByStreamIdAndTickLessThanEqualOrderByStreamVersionAsc(String streamId, long tick);

    Optional<EventDocument> findTopByStreamIdOrderByStreamVersionDesc(String streamId);

    void deleteByStreamIdAndTickGreaterThan(String streamId, long tick);

    long countByStreamId(String streamId);
}
