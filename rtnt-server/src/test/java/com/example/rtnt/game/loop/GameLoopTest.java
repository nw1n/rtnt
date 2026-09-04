package com.example.rtnt.game.loop;

import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.FlowMode;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusDocument;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusMongoRepository;
import com.example.rtnt.game.core.loop.GameCommand;
import com.example.rtnt.game.core.loop.GameCommandQueue;
import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.core.ticker.persistence.TickerDocument;
import com.example.rtnt.game.core.ticker.persistence.TickerMongoRepository;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.domain.TradePriceList;
import com.example.rtnt.game.island.service.IslandEconomy;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.service.ShipJourneyCheck;
import com.example.rtnt.game.ship.service.ShipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private TickerMongoRepository tickerMongoRepository;

    @Mock
    private GameFlowStatusMongoRepository gameFlowStatusMongoRepository;

    @Mock
    private WorldSnapshotStore worldSnapshotStore;

    @Mock
    private IslandService islandService;

    @Mock
    private IslandEconomy islandEconomy;

    @Mock
    private ShipJourneyCheck shipJourneyCheck;

    @Mock
    private ShipService shipService;

    private GameCommandQueue gameCommandQueue;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        this.gameCommandQueue = new GameCommandQueue();
        lenient().when(this.islandService.list()).thenReturn(List.of());
        lenient().when(this.islandService.listStatuses()).thenReturn(List.of());
        lenient().when(this.islandEconomy.applyIfDue(org.mockito.ArgumentMatchers.anyLong())).thenReturn(false);
        lenient().when(this.shipJourneyCheck.applyIfDue(org.mockito.ArgumentMatchers.anyLong())).thenReturn(false);
        lenient().when(this.shipService.list()).thenReturn(List.of());
        this.gameLoop = new GameLoop(
                this.tickerMongoRepository,
                this.gameFlowStatusMongoRepository,
                this.gameCommandQueue,
                this.worldSnapshotStore,
                this.islandService,
                this.islandEconomy,
                this.shipJourneyCheck,
                this.shipService,
                2,
                1000
        );
    }

    @Test
    void startsInBatchPausedWhenNoFlowDocument() {
        when(this.tickerMongoRepository.findById(TickerDocument.DOCUMENT_ID)).thenReturn(Optional.empty());
        when(this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        GameFlowStatus status = this.gameLoop.get();

        assertEquals(0, status.tick());
        assertEquals(FlowMode.BATCH, status.mode());
        assertTrue(status.paused());
        assertEquals(1000, status.liveIntervalMs());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(FlowMode.BATCH, saved.mode());
        assertTrue(saved.paused());
        assertEquals(1000, saved.liveIntervalMs());
    }

    @Test
    void stepAdvancesTickAndDrainsCommandsForThatTick() {
        this.givenLatest(0, FlowMode.BATCH, false);
        this.gameCommandQueue.enqueue(0, new GameCommand("depart"));
        this.gameCommandQueue.enqueue(1, new GameCommand("later"));

        GameFlowStatus status = this.gameLoop.step();

        assertEquals(1, status.tick());
        assertTrue(this.gameCommandQueue.drain(0).isEmpty());
        assertEquals(1, this.gameCommandQueue.drain(1).size());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void quietLiveTickDoesNotPersistLatest() {
        this.givenLatest(0, FlowMode.LIVE, false);

        this.gameLoop.stepIfLive();

        assertEquals(1, this.gameLoop.get().tick());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void stepIfLiveDoesNothingWhenPaused() {
        this.givenLatest(0, FlowMode.LIVE, true);

        this.gameLoop.stepIfLive();

        assertEquals(0, this.gameLoop.get().tick());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceQuietTicksDoesNotPersistUntilSnapshot() {
        this.givenLatest(0, FlowMode.BATCH, false);

        GameFlowStatus status = this.gameLoop.advance(1);

        assertEquals(1, status.tick());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void islandEconomyDoesNotPersistBetweenSnapshots() {
        this.givenLatest(2, FlowMode.BATCH, false);
        when(this.islandEconomy.applyIfDue(3)).thenReturn(true);

        this.gameLoop.step();

        assertEquals(3, this.gameLoop.get().tick());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shipJourneyCheckDoesNotPersistBetweenSnapshots() {
        this.givenLatest(2, FlowMode.BATCH, false);
        when(this.shipJourneyCheck.applyIfDue(3)).thenReturn(true);

        this.gameLoop.step();

        assertEquals(3, this.gameLoop.get().tick());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRejectsNonPositiveTicks() {
        assertThrows(IllegalArgumentException.class, () -> this.gameLoop.advance(0));
    }

    @Test
    void snapshotIfAtTickZeroPersistsWorld() {
        this.givenLatest(0, FlowMode.LIVE, false);

        this.gameLoop.snapshotIfAtTickZero();

        ArgumentCaptor<WorldSnapshot> captor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(this.worldSnapshotStore).save(captor.capture());
        assertEquals(0, captor.getValue().tick());
        assertEquals(List.of(), captor.getValue().ships());
        assertEquals(0, this.capturedTicker().tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void snapshotIfAtTickZeroSkippedWhenTickMoved() {
        this.givenLatest(1, FlowMode.LIVE, false);

        this.gameLoop.snapshotIfAtTickZero();

        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void writesWorldSnapshotAndTickerOnInterval() {
        this.givenLatest(0, FlowMode.LIVE, false);

        this.gameLoop.advance(4);

        ArgumentCaptor<WorldSnapshot> snapshotCaptor = ArgumentCaptor.forClass(WorldSnapshot.class);
        verify(this.worldSnapshotStore, times(2)).save(snapshotCaptor.capture());
        assertEquals(2, snapshotCaptor.getAllValues().get(0).tick());
        assertEquals(4, snapshotCaptor.getAllValues().get(1).tick());

        ArgumentCaptor<TickerDocument> tickerCaptor = ArgumentCaptor.forClass(TickerDocument.class);
        verify(this.tickerMongoRepository, times(2)).save(tickerCaptor.capture());
        assertEquals(4, tickerCaptor.getAllValues().get(1).tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void pauseUpdatesFlagWithoutTicking() {
        this.givenLatest(12, FlowMode.LIVE, false);

        GameFlowStatus paused = this.gameLoop.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertTrue(saved.paused());
        assertEquals(FlowMode.LIVE, saved.mode());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void setModeBatchDoesNotPersist() {
        this.givenLatest(0, FlowMode.LIVE, false);

        GameFlowStatus status = this.gameLoop.setMode(FlowMode.BATCH);

        assertEquals(FlowMode.BATCH, status.mode());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void setLiveIntervalMsPersistsInBatch() {
        this.givenLatest(0, FlowMode.BATCH, true);

        GameFlowStatus status = this.gameLoop.setLiveIntervalMs(250);

        assertEquals(250, status.liveIntervalMs());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(250, saved.liveIntervalMs());
        assertEquals(FlowMode.BATCH, saved.mode());
        assertTrue(saved.paused());
    }

    @Test
    void stepIfLiveSkipsUntilIntervalElapsed() {
        this.givenLatest(0, FlowMode.LIVE, false, 60_000);

        this.gameLoop.stepIfLive();
        this.gameLoop.stepIfLive();

        assertEquals(1, this.gameLoop.get().tick());
    }

    @Test
    void setModeLiveUnpausesAndPersists() {
        this.givenLatest(0, FlowMode.BATCH, true);

        GameFlowStatus status = this.gameLoop.setMode(FlowMode.LIVE);

        assertEquals(FlowMode.LIVE, status.mode());
        assertFalse(status.paused());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(FlowMode.LIVE, saved.mode());
        assertFalse(saved.paused());
        assertEquals(1000, saved.liveIntervalMs());
    }

    @Test
    void readsMongoOnceThenUsesMemory() {
        this.givenLatest(0, FlowMode.LIVE, false);

        this.gameLoop.get();
        this.gameLoop.step();
        this.gameLoop.stepIfLive();

        verify(this.tickerMongoRepository, times(1)).findById(TickerDocument.DOCUMENT_ID);
        verify(this.gameFlowStatusMongoRepository, times(1)).findById(GameFlowStatusDocument.DOCUMENT_ID);
        assertEquals(2, this.capturedTicker().tick());
    }

    @Test
    void loadFromSnapshotRestoresWorldAndPauses() {
        this.givenLatest(500, FlowMode.LIVE, false);
        Island island = Island.existing("i1", "North", new Footprint(1, 2, 10, 12));
        IslandStatus status = new IslandStatus("i1", 42, Inventory.empty(), TradePriceList.defaultPrices());
        Ship ship = Ship.create("Black Pearl", island.id(), null);
        WorldSnapshot snapshot = new WorldSnapshot(200, List.of(island), List.of(status), List.of(ship));
        lenient().when(this.worldSnapshotStore.findByTick(500)).thenReturn(Optional.empty());
        when(this.worldSnapshotStore.findByTick(200)).thenReturn(Optional.of(snapshot));
        this.gameCommandQueue.enqueue(10, new GameCommand("stale"));

        GameFlowStatus restored = this.gameLoop.loadFromSnapshot(200);

        assertEquals(200, restored.tick());
        assertEquals(FlowMode.LIVE, restored.mode());
        assertTrue(restored.paused());
        verify(this.islandService).replaceAll(snapshot.islands(), snapshot.islandStatuses());
        verify(this.shipService).replaceAll(snapshot.ships());
        assertEquals(200, this.capturedTicker().tick());
        assertTrue(this.capturedFlow().paused());
        assertTrue(this.gameCommandQueue.drain(10).isEmpty());
    }

    @Test
    void loadFromSnapshotRejectsMissingTick() {
        this.givenLatest(0, FlowMode.BATCH, true);
        lenient().when(this.worldSnapshotStore.findByTick(0)).thenReturn(Optional.empty());
        when(this.worldSnapshotStore.findByTick(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> this.gameLoop.loadFromSnapshot(99));
        verify(this.islandService, never()).replaceAll(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
        verify(this.tickerMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void givenLatest(long tick, FlowMode mode, boolean paused) {
        this.givenLatest(tick, mode, paused, null);
    }

    private void givenLatest(long tick, FlowMode mode, boolean paused, Integer liveIntervalMs) {
        when(this.tickerMongoRepository.findById(TickerDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new TickerDocument(TickerDocument.DOCUMENT_ID, tick)));
        when(this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new GameFlowStatusDocument(
                        GameFlowStatusDocument.DOCUMENT_ID,
                        mode,
                        paused,
                        liveIntervalMs
                )));
    }

    private TickerDocument capturedTicker() {
        ArgumentCaptor<TickerDocument> captor = ArgumentCaptor.forClass(TickerDocument.class);
        verify(this.tickerMongoRepository).save(captor.capture());
        return captor.getValue();
    }

    private GameFlowStatusDocument capturedFlow() {
        ArgumentCaptor<GameFlowStatusDocument> captor = ArgumentCaptor.forClass(GameFlowStatusDocument.class);
        verify(this.gameFlowStatusMongoRepository).save(captor.capture());
        return captor.getValue();
    }
}
