package com.example.rtnt.game.core.worldsnapshot.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorldSnapshotMongoRepository extends MongoRepository<WorldSnapshotDocument, Long> {
    List<WorldSnapshotDocument> findAllByOrderByTickAsc();

    Optional<WorldSnapshotDocument> findFirstByOrderByTickDesc();
}
