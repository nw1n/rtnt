package com.example.rtnt.game.core.worldsnapshot.persistence;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@NullMarked
public class MongoWorldSnapshotStore implements WorldSnapshotStore {
    private final WorldSnapshotMongoRepository worldSnapshotMongoRepository;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public MongoWorldSnapshotStore(WorldSnapshotMongoRepository worldSnapshotMongoRepository) {
        this.worldSnapshotMongoRepository = worldSnapshotMongoRepository;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public void save(WorldSnapshot snapshot) {
        this.worldSnapshotMongoRepository.save(WorldSnapshotDocument.from(snapshot));
    }

    @Override
    public boolean exists(long tick) {
        return this.worldSnapshotMongoRepository.existsById(tick);
    }

    @Override
    public Optional<WorldSnapshot> findByTick(long tick) {
        return this.worldSnapshotMongoRepository.findById(tick).map(WorldSnapshotDocument::toSnapshot);
    }

    @Override
    public Optional<WorldSnapshot> findLatest() {
        return this.worldSnapshotMongoRepository.findFirstByOrderByTickDesc().map(WorldSnapshotDocument::toSnapshot);
    }

    @Override
    public List<WorldSnapshot> list() {
        return this.worldSnapshotMongoRepository.findAllByOrderByTickAsc().stream()
                .map(WorldSnapshotDocument::toSnapshot)
                .toList();
    }
}
