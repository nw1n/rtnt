package com.example.rtnt.game.loop;

import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.TimeMode;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusDocument;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusMongoRepository;
import com.example.rtnt.game.core.loop.GameCommand;
import com.example.rtnt.game.core.loop.GameCommandQueue;
import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.island.service.IslandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLoopTest {

    @Mock
    private GameFlowStatusMongoRepository gameFlowStatusMongoRepository;

    @Mock
    private WorldSnapshotStore worldSnapshotStore;

    @Mock
    private IslandService islandService;

    private GameCommandQueue gameCommandQueue;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        this.gameCommandQueue = new GameCommandQueue();
        lenient().when(this.islandService.list()).thenReturn(List.of());
        this.gameLoop = new GameLoop(
                this.gameFlowStatusMongoRepository,
                this.gameCommandQueue,
                this.worldSnapshotStore,
                this.islandService,
                2
        );
    }

    @Test
    void stepAdvancesTickAndDrainsCommandsForThatTick() {
        this.givenLatest(0, TimeMode.BATCH, false);
        this.gameCommandQueue.enqueue(0, new GameCommand("depart"));
        this.gameCommandQueue.enqueue(1, new GameCommand("later"));

        GameFlowStatus status = this.gameLoop.step();

        assertEquals(1, status.tick());
        assertTrue(this.gameCommandQueue.drain(0).isEmpty());
        assertEquals(1, this.gameCommandQueue.drain(1).size());
        GameFlowStatusDocument saved = this.capturedLatest();
        assertEquals(1, saved.tick());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void quietLiveTickDoesNotPersistLatest() {
        this.givenLatest(0, TimeMode.LIVE, false);

        this.gameLoop.stepIfLive();

        assertEquals(1, this.gameLoop.get().tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void stepIfLiveDoesNothingWhenPaused() {
        this.givenLatest(0, TimeMode.LIVE, true);

        this.gameLoop.stepIfLive();

        assertEquals(0, this.gameLoop.get().tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceQuietTicksDoesNotPersistUntilSnapshot() {
        this.givenLatest(0, TimeMode.BATCH, false);

        GameFlowStatus status = this.gameLoop.advance(1);

        assertEquals(1, status.tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRejectsNonPositiveTicks() {
        assertThrows(IllegalArgumentException.class, () -> this.gameLoop.advance(0));
    }

    @Test
    void snapshotIfAtTickZeroPersistsWorld() {
        this.givenLatest(0, TimeMode.LIVE, false);

        this.gameLoop.snapshotIfAtTickZero();

        ArgumentCaptor<WorldSnapshot> captor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(this.worldSnapshotStore).save(captor.capture());
        assertEquals(0, captor.getValue().tick());
        assertEquals(0, this.capturedLatest().tick());
    }

    @Test
    void snapshotIfAtTickZeroSkippedWhenTickMoved() {
        this.givenLatest(1, TimeMode.LIVE, false);

        this.gameLoop.snapshotIfAtTickZero();

        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void writesWorldSnapshotAndLatestOnInterval() {
        this.givenLatest(0, TimeMode.LIVE, false);

        this.gameLoop.advance(4);

        ArgumentCaptor<WorldSnapshot> snapshotCaptor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(this.worldSnapshotStore, times(2)).save(snapshotCaptor.capture());
        assertEquals(2, snapshotCaptor.getAllValues().get(0).tick());
        assertEquals(4, snapshotCaptor.getAllValues().get(1).tick());

        ArgumentCaptor<GameFlowStatusDocument> latestCaptor = ArgumentCaptor.forClass(GameFlowStatusDocument.class);
        verify(this.gameFlowStatusMongoRepository, times(2)).save(latestCaptor.capture());
        assertEquals(4, latestCaptor.getAllValues().get(1).tick());
    }

    @Test
    void pauseUpdatesFlagWithoutTicking() {
        this.givenLatest(12, TimeMode.LIVE, false);

        GameFlowStatus paused = this.gameLoop.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
        GameFlowStatusDocument saved = this.capturedLatest();
        assertTrue(saved.paused());
        assertEquals(12, saved.tick());
    }

    @Test
    void setModeBatchDoesNotPersist() {
        this.givenLatest(0, TimeMode.LIVE, false);

        GameFlowStatus status = this.gameLoop.setMode(TimeMode.BATCH);

        assertEquals(TimeMode.BATCH, status.mode());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void readsMongoOnceThenUsesMemory() {
        this.givenLatest(0, TimeMode.LIVE, false);

        this.gameLoop.get();
        this.gameLoop.step();
        this.gameLoop.stepIfLive();

        verify(this.gameFlowStatusMongoRepository, times(1)).findById(GameFlowStatusDocument.DOCUMENT_ID);
        assertEquals(2, this.capturedLatest().tick());
    }

    private void givenLatest(long tick, TimeMode mode, boolean paused) {
        when(this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new GameFlowStatusDocument(
                        GameFlowStatusDocument.DOCUMENT_ID,
                        tick,
                        mode,
                        paused
                )));
    }

    private GameFlowStatusDocument capturedLatest() {
        ArgumentCaptor<GameFlowStatusDocument> captor = ArgumentCaptor.forClass(GameFlowStatusDocument.class);
        verify(this.gameFlowStatusMongoRepository).save(captor.capture());
        return captor.getValue();
    }
}
