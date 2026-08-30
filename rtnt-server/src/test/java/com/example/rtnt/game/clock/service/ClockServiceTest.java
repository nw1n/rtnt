package com.example.rtnt.game.clock.service;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClockServiceTest {

    @Mock
    private GameClockMongoRepository gameClockMongoRepository;

    private ClockService clockService;

    @BeforeEach
    void setUp() {
        this.clockService = new ClockService(this.gameClockMongoRepository);
    }

    @Test
    void replaceUnderLockPersistsWhenLiveTickChanges() {
        this.givenClock(GameClock.initial());

        this.clockService.replaceUnderLock(GameClock::advance);

        GameClock saved = this.capturedSave();
        assertEquals(1, saved.tick());
        assertEquals(ClockMode.LIVE, saved.mode());
    }

    @Test
    void replaceUnderLockDoesNotPersistWhenTickUnchanged() {
        this.givenClock(GameClock.initial());

        this.clockService.replaceUnderLock(clock -> clock);

        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void replaceUnderLockDoesNotPersistInBatchMode() {
        this.givenClock(GameClock.initial().withMode(ClockMode.BATCH));

        GameClock clock = this.clockService.replaceUnderLock(GameClock::advance);

        assertEquals(1, clock.tick());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void pauseAndResumeUpdateFlagWithoutTicking() {
        this.givenClock(new GameClock(12, ClockMode.LIVE, false));

        GameClock paused = this.clockService.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
        GameClock saved = this.capturedSave();
        assertTrue(saved.paused());
        assertEquals(12, saved.tick());
    }

    @Test
    void setModeBatchDoesNotPersist() {
        this.givenClock(GameClock.initial());

        GameClock clock = this.clockService.setMode(ClockMode.BATCH);

        assertEquals(ClockMode.BATCH, clock.mode());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void readsMongoOnceThenUsesMemory() {
        this.givenClock(GameClock.initial());

        this.clockService.get();
        this.clockService.replaceUnderLock(GameClock::advance);

        verify(this.gameClockMongoRepository, times(1)).findById(GameClockDocument.DOCUMENT_ID);
        assertEquals(1, this.capturedSave().tick());
    }

    private void givenClock(GameClock clock) {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(clock)));
    }

    private GameClock capturedSave() {
        ArgumentCaptor<GameClockDocument> captor = ArgumentCaptor.forClass(GameClockDocument.class);
        verify(this.gameClockMongoRepository).save(captor.capture());
        return captor.getValue().toClock();
    }
}
