package com.example.rtnt.game.core.worldsnapshot.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldSnapshotMongoRepository extends MongoRepository<WorldSnapshotDocument, Long> {
    List<WorldSnapshotDocument> findAllByOrderByTickAsc();
}
