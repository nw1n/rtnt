package com.example.rtnt.game.loop;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLoopTest {

    @Mock
    private GameClockMongoRepository gameClockMongoRepository;

    @Mock
    private WorldSnapshotStore worldSnapshotStore;

    private GameCommandQueue gameCommandQueue;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        this.gameCommandQueue = new GameCommandQueue();
        this.gameLoop = new GameLoop(
                this.gameClockMongoRepository,
                this.gameCommandQueue,
                this.worldSnapshotStore,
                2
        );
    }

    @Test
    void stepAdvancesClockAndDrainsCommandsForThatTick() {
        this.givenLatest(0, ClockMode.BATCH, false);
        this.gameCommandQueue.enqueue(0, new GameCommand("depart"));
        this.gameCommandQueue.enqueue(1, new GameCommand("later"));

        ClockStatus status = this.gameLoop.step();

        assertEquals(1, status.tick());
        assertTrue(this.gameCommandQueue.drain(0).isEmpty());
        assertEquals(1, this.gameCommandQueue.drain(1).size());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void stepIfLivePersists() {
        this.givenLatest(0, ClockMode.LIVE, false);

        this.gameLoop.stepIfLive();

        GameClockDocument saved = this.capturedLatest();
        assertEquals(1, saved.tick());
        assertEquals(ClockMode.LIVE, saved.mode());
    }

    @Test
    void stepIfLiveDoesNothingWhenPaused() {
        this.givenLatest(0, ClockMode.LIVE, true);

        this.gameLoop.stepIfLive();

        assertEquals(0, this.gameLoop.get().tick());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRunsStepNTimesWithoutPersistingLatest() {
        this.givenLatest(1_000, ClockMode.BATCH, false);

        ClockStatus status = this.gameLoop.advance(3);

        assertEquals(1_003, status.tick());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRejectsNonPositiveTicks() {
        assertThrows(IllegalArgumentException.class, () -> this.gameLoop.advance(0));
    }

    @Test
    void writesSnapshotOnInterval() {
        this.givenLatest(0, ClockMode.LIVE, false);

        this.gameLoop.advance(4);

        ArgumentCaptor<GameClock> captor = ArgumentCaptor.forClass(GameClock.class);
        verify(this.worldSnapshotStore, times(2)).save(captor.capture());
        assertEquals(2, captor.getAllValues().get(0).tick());
        assertEquals(4, captor.getAllValues().get(1).tick());
    }

    @Test
    void pauseUpdatesFlagWithoutTicking() {
        this.givenLatest(12, ClockMode.LIVE, false);

        ClockStatus paused = this.gameLoop.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
        GameClockDocument saved = this.capturedLatest();
        assertTrue(saved.paused());
        assertEquals(12, saved.tick());
    }

    @Test
    void setModeBatchDoesNotPersist() {
        this.givenLatest(0, ClockMode.LIVE, false);

        ClockStatus status = this.gameLoop.setMode(ClockMode.BATCH);

        assertEquals(ClockMode.BATCH, status.mode());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void readsMongoOnceThenUsesMemory() {
        this.givenLatest(0, ClockMode.LIVE, false);

        this.gameLoop.get();
        this.gameLoop.step();
        this.gameLoop.stepIfLive();

        verify(this.gameClockMongoRepository, times(1)).findById(GameClockDocument.DOCUMENT_ID);
        assertEquals(2, this.capturedLatest().tick());
    }

    private void givenLatest(long tick, ClockMode mode, boolean paused) {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new GameClockDocument(
                        GameClockDocument.DOCUMENT_ID,
                        tick,
                        mode,
                        paused
                )));
    }

    private GameClockDocument capturedLatest() {
        ArgumentCaptor<GameClockDocument> captor = ArgumentCaptor.forClass(GameClockDocument.class);
        verify(this.gameClockMongoRepository).save(captor.capture());
        return captor.getValue();
    }
}
