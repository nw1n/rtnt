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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
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
    void tickIfLiveAdvancesWhenLiveAndUnpaused() {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(GameClock.initial())));

        this.clockService.tickIfLive();

        GameClock saved = this.capturedSave();
        assertEquals(1, saved.tick());
        assertEquals(ClockMode.LIVE, saved.mode());
        assertFalse(saved.paused());
    }

    @Test
    void tickIfLiveDoesNothingWhenPaused() {
        GameClock paused = GameClock.initial().pause();
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(paused)));

        this.clockService.tickIfLive();

        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void tickIfLiveDoesNothingInBatchMode() {
        GameClock batch = GameClock.initial().withMode(ClockMode.BATCH);
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(batch)));

        this.clockService.tickIfLive();

        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advancePersistsOnceAtTheEnd() {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(GameClock.initial())));

        GameClock clock = this.clockService.advance(3);

        assertEquals(3, clock.tick());
        GameClock saved = this.capturedSave();
        assertEquals(3, saved.tick());
    }

    @Test
    void pauseAndResumeUpdateFlagWithoutTicking() {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(new GameClock(12, ClockMode.LIVE, false))));

        GameClock paused = this.clockService.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
    }

    private GameClock capturedSave() {
        ArgumentCaptor<GameClockDocument> captor = ArgumentCaptor.forClass(GameClockDocument.class);
        verify(this.gameClockMongoRepository).save(captor.capture());
        return captor.getValue().toClock();
    }
}
