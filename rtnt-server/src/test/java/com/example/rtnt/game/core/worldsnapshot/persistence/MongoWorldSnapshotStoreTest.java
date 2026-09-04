package com.example.rtnt.game.core.worldsnapshot.persistence;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoWorldSnapshotStoreTest {

    @Mock
    private WorldSnapshotMongoRepository worldSnapshotMongoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private BulkOperations bulkOperations;

    @BeforeEach
    void setUp() {
        lenient().when(this.mongoTemplate.bulkOps(eq(BulkOperations.BulkMode.UNORDERED), eq(WorldSnapshotDocument.class)))
                .thenReturn(this.bulkOperations);
        lenient().when(this.bulkOperations.insert(anyList())).thenReturn(this.bulkOperations);
    }

    @Test
    void keepsSnapshotsInMemoryUntilFlushEvery() {
        MongoWorldSnapshotStore store = this.store(3);
        store.save(this.snapshot(100));
        store.save(this.snapshot(200));
        store.flushIfDue();

        verify(this.mongoTemplate, never()).bulkOps(eq(BulkOperations.BulkMode.UNORDERED), eq(WorldSnapshotDocument.class));
        assertTrue(store.exists(100));
        assertEquals(100, store.findByTick(100).orElseThrow().tick());
        assertEquals(200, store.findLatest().orElseThrow().tick());
        assertEquals(List.of(100L, 200L), store.list().stream().map(WorldSnapshot::tick).toList());
    }

    @Test
    void writesWhenFlushEveryIsReached() {
        MongoWorldSnapshotStore store = this.store(2);
        store.save(this.snapshot(100));
        store.save(this.snapshot(200));
        store.flushIfDue();

        verify(this.bulkOperations).insert(anyList());
        verify(this.bulkOperations).execute();
        when(this.worldSnapshotMongoRepository.findFirstByOrderByTickDesc()).thenReturn(Optional.empty());
        assertTrue(store.findLatest().isEmpty());
    }

    @Test
    void flushWritesRemainingSnapshots() {
        MongoWorldSnapshotStore store = this.store(10);
        store.save(this.snapshot(100));
        store.flush();

        verify(this.bulkOperations, times(1)).execute();
        store.flush();
        verify(this.bulkOperations, times(1)).execute();
    }

    @Test
    void listOverlaysPendingOnPersistedSnapshots() {
        when(this.worldSnapshotMongoRepository.findAllByOrderByTickAsc()).thenReturn(List.of(
                WorldSnapshotDocument.from(this.snapshot(0))
        ));
        MongoWorldSnapshotStore store = this.store(10);
        store.save(this.snapshot(100));

        assertEquals(List.of(0L, 100L), store.list().stream().map(WorldSnapshot::tick).toList());
    }

    private MongoWorldSnapshotStore store(int flushEvery) {
        return new MongoWorldSnapshotStore(this.worldSnapshotMongoRepository, this.mongoTemplate, flushEvery);
    }

    private WorldSnapshot snapshot(long tick) {
        return new WorldSnapshot(tick, List.of(), List.of(), List.of());
    }
}
