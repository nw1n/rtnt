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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private GameCommandQueue gameCommandQueue;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        this.gameCommandQueue = new GameCommandQueue();
        this.gameLoop = new GameLoop(
                this.tickerMongoRepository,
                this.gameFlowStatusMongoRepository,
                this.gameCommandQueue
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
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(FlowMode.BATCH, saved.mode());
        assertTrue(saved.paused());
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
        assertEquals(1, this.capturedTicker().tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void liveTickPersistsTicker() {
        this.givenLatest(0, FlowMode.LIVE, false);

        this.gameLoop.stepIfLive();

        assertEquals(1, this.gameLoop.get().tick());
        assertEquals(1, this.capturedTicker().tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
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
    void advancePersistsOnceAtTheEnd() {
        this.givenLatest(0, FlowMode.BATCH, false);

        GameFlowStatus status = this.gameLoop.advance(3);

        assertEquals(3, status.tick());
        assertEquals(3, this.capturedTicker().tick());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRejectsNonPositiveTicks() {
        assertThrows(IllegalArgumentException.class, () -> this.gameLoop.advance(0));
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
    void setModeLiveUnpausesAndPersists() {
        this.givenLatest(0, FlowMode.BATCH, true);

        GameFlowStatus status = this.gameLoop.setMode(FlowMode.LIVE);

        assertEquals(FlowMode.LIVE, status.mode());
        assertFalse(status.paused());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(FlowMode.LIVE, saved.mode());
        assertFalse(saved.paused());
    }

    @Test
    void readsMongoOnceThenUsesMemory() {
        this.givenLatest(0, FlowMode.LIVE, false);

        this.gameLoop.get();
        this.gameLoop.step();
        this.gameLoop.stepIfLive();

        verify(this.tickerMongoRepository, times(1)).findById(TickerDocument.DOCUMENT_ID);
        verify(this.gameFlowStatusMongoRepository, times(1)).findById(GameFlowStatusDocument.DOCUMENT_ID);
        verify(this.tickerMongoRepository, times(2)).save(org.mockito.ArgumentMatchers.any());
        assertEquals(2, this.gameLoop.get().tick());
    }

    private void givenLatest(long tick, FlowMode mode, boolean paused) {
        when(this.tickerMongoRepository.findById(TickerDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new TickerDocument(TickerDocument.DOCUMENT_ID, tick)));
        when(this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new GameFlowStatusDocument(
                        GameFlowStatusDocument.DOCUMENT_ID,
                        mode,
                        paused
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
