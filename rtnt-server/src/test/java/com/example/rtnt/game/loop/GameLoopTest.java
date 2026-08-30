package com.example.rtnt.game.loop;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import com.example.rtnt.game.clock.service.ClockService;
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
    private ClockService clockService;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        this.gameCommandQueue = new GameCommandQueue();
        this.clockService = new ClockService(this.gameClockMongoRepository);
        this.gameLoop = new GameLoop(this.clockService, this.gameCommandQueue, this.worldSnapshotStore, 2);
    }

    @Test
    void stepAdvancesClockAndDrainsCommandsForThatTick() {
        this.givenClock(GameClock.initial());
        this.gameCommandQueue.enqueue(0, new GameCommand("depart"));
        this.gameCommandQueue.enqueue(1, new GameCommand("later"));

        GameClock clock = this.gameLoop.step();

        assertEquals(1, clock.tick());
        assertTrue(this.gameCommandQueue.drain(0).isEmpty());
        assertEquals(1, this.gameCommandQueue.drain(1).size());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.worldSnapshotStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void stepIfLivePersists() {
        this.givenClock(GameClock.initial());

        this.gameLoop.stepIfLive();

        GameClock saved = this.capturedLatest();
        assertEquals(1, saved.tick());
    }

    @Test
    void stepIfLiveDoesNothingWhenPaused() {
        this.givenClock(GameClock.initial().pause());

        this.gameLoop.stepIfLive();

        assertEquals(0, this.clockService.get().tick());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRunsStepNTimesWithoutPersistingLatest() {
        this.givenClock(new GameClock(1_000, ClockMode.BATCH, false));

        GameClock clock = this.gameLoop.advance(3);

        assertEquals(1_003, clock.tick());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceRejectsNonPositiveTicks() {
        assertThrows(IllegalArgumentException.class, () -> this.gameLoop.advance(0));
    }

    @Test
    void writesSnapshotOnInterval() {
        this.givenClock(GameClock.initial());

        this.gameLoop.advance(4);

        ArgumentCaptor<GameClock> captor = ArgumentCaptor.forClass(GameClock.class);
        verify(this.worldSnapshotStore, times(2)).save(captor.capture());
        assertEquals(2, captor.getAllValues().get(0).tick());
        assertEquals(4, captor.getAllValues().get(1).tick());
    }

    private void givenClock(GameClock clock) {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(clock)));
    }

    private GameClock capturedLatest() {
        ArgumentCaptor<GameClockDocument> captor = ArgumentCaptor.forClass(GameClockDocument.class);
        verify(this.gameClockMongoRepository).save(captor.capture());
        return captor.getValue().toClock();
    }
}
