package com.example.rtnt.game.core.worldsnapshot.persistence;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoWorldSnapshotStoreTest {

    @Mock
    private WorldSnapshotMongoRepository worldSnapshotMongoRepository;

    @Test
    void keepsSnapshotsInMemoryUntilFlushEvery() {
        MongoWorldSnapshotStore store = new MongoWorldSnapshotStore(this.worldSnapshotMongoRepository, 3);
        store.save(this.snapshot(100));
        store.save(this.snapshot(200));

        verify(this.worldSnapshotMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
        assertTrue(store.exists(100));
        assertEquals(100, store.findByTick(100).orElseThrow().tick());
        assertEquals(200, store.findLatest().orElseThrow().tick());
        assertEquals(List.of(100L, 200L), store.list().stream().map(WorldSnapshot::tick).toList());
    }

    @Test
    void writesWhenFlushEveryIsReached() {
        MongoWorldSnapshotStore store = new MongoWorldSnapshotStore(this.worldSnapshotMongoRepository, 2);
        store.save(this.snapshot(100));
        store.save(this.snapshot(200));

        ArgumentCaptor<List<WorldSnapshotDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.worldSnapshotMongoRepository).saveAll(captor.capture());
        assertEquals(List.of(100L, 200L), captor.getValue().stream().map(WorldSnapshotDocument::tick).toList());
        when(this.worldSnapshotMongoRepository.findFirstByOrderByTickDesc()).thenReturn(Optional.empty());
        assertTrue(store.findLatest().isEmpty());
    }

    @Test
    void flushWritesRemainingSnapshots() {
        MongoWorldSnapshotStore store = new MongoWorldSnapshotStore(this.worldSnapshotMongoRepository, 10);
        store.save(this.snapshot(100));
        store.flush();

        verify(this.worldSnapshotMongoRepository, times(1)).saveAll(org.mockito.ArgumentMatchers.any());
        store.flush();
        verify(this.worldSnapshotMongoRepository, times(1)).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listOverlaysPendingOnPersistedSnapshots() {
        when(this.worldSnapshotMongoRepository.findAllByOrderByTickAsc()).thenReturn(List.of(
                WorldSnapshotDocument.from(this.snapshot(0))
        ));
        MongoWorldSnapshotStore store = new MongoWorldSnapshotStore(this.worldSnapshotMongoRepository, 10);
        store.save(this.snapshot(100));

        assertEquals(List.of(0L, 100L), store.list().stream().map(WorldSnapshot::tick).toList());
    }

    private WorldSnapshot snapshot(long tick) {
        return new WorldSnapshot(tick, List.of(), List.of(), List.of());
    }
}
