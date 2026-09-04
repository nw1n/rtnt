package com.example.rtnt.game.core.worldsnapshot.persistence;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@NullMarked
public class MongoWorldSnapshotStore implements WorldSnapshotStore {
    private final WorldSnapshotMongoRepository worldSnapshotMongoRepository;
    private final MongoTemplate mongoTemplate;
    private final int flushEvery;
    private final LinkedHashMap<Long, WorldSnapshot> pending = new LinkedHashMap<>();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public MongoWorldSnapshotStore(
            WorldSnapshotMongoRepository worldSnapshotMongoRepository,
            MongoTemplate mongoTemplate,
            @Value("${rtnt.snapshot.flush-every:10}") int flushEvery
    ) {
        if (flushEvery < 1) {
            throw new IllegalArgumentException("flushEvery must be at least 1");
        }
        this.worldSnapshotMongoRepository = worldSnapshotMongoRepository;
        this.mongoTemplate = mongoTemplate;
        this.flushEvery = flushEvery;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public synchronized void save(WorldSnapshot snapshot) {
        this.pending.put(snapshot.tick(), snapshot);
    }

    @Override
    public synchronized void flushIfDue() {
        if (this.pending.size() >= this.flushEvery) {
            this.flush();
        }
    }

    @Override
    public synchronized void flush() {
        if (this.pending.isEmpty()) {
            return;
        }
        List<WorldSnapshotDocument> documents = this.pending.values().stream()
                .map(WorldSnapshotDocument::from)
                .toList();
        this.mongoTemplate
                .bulkOps(BulkOperations.BulkMode.UNORDERED, WorldSnapshotDocument.class)
                .insert(documents)
                .execute();
        this.pending.clear();
    }

    @Override
    public synchronized boolean exists(long tick) {
        return this.pending.containsKey(tick) || this.worldSnapshotMongoRepository.existsById(tick);
    }

    @Override
    public synchronized Optional<WorldSnapshot> findByTick(long tick) {
        WorldSnapshot buffered = this.pending.get(tick);
        if (buffered != null) {
            return Optional.of(buffered);
        }
        return this.worldSnapshotMongoRepository.findById(tick).map(WorldSnapshotDocument::toSnapshot);
    }

    @Override
    public synchronized Optional<WorldSnapshot> findLatest() {
        Optional<WorldSnapshot> persisted = this.worldSnapshotMongoRepository
                .findFirstByOrderByTickDesc()
                .map(WorldSnapshotDocument::toSnapshot);
        if (this.pending.isEmpty()) {
            return persisted;
        }
        WorldSnapshot buffered = this.pending.sequencedValues().getLast();
        if (persisted.isEmpty() || buffered.tick() >= persisted.get().tick()) {
            return Optional.of(buffered);
        }
        return persisted;
    }

    @Override
    public synchronized List<WorldSnapshot> list() {
        Map<Long, WorldSnapshot> byTick = new LinkedHashMap<>();
        for (WorldSnapshotDocument document : this.worldSnapshotMongoRepository.findAllByOrderByTickAsc()) {
            byTick.put(document.tick(), document.toSnapshot());
        }
        byTick.putAll(this.pending);
        List<WorldSnapshot> snapshots = new ArrayList<>(byTick.values());
        snapshots.sort(Comparator.comparingLong(WorldSnapshot::tick));
        return List.copyOf(snapshots);
    }
}
