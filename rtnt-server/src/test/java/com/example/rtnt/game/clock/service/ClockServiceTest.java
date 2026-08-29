package com.example.rtnt.game.clock.service;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.event.ClockPausedEvent;
import com.example.rtnt.game.clock.event.TickAdvancedEvent;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import com.example.rtnt.game.log.domain.GameLogType;
import com.example.rtnt.game.log.persistence.GameLogDocument;
import com.example.rtnt.game.log.persistence.GameLogMongoRepository;
import com.example.rtnt.game.log.service.GameLogService;
import com.example.rtnt.game.log.service.TickLogSystem;
import com.example.rtnt.game.system.GameUnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClockServiceTest {

    @Mock
    private GameClockMongoRepository gameClockMongoRepository;

    @Mock
    private GameLogMongoRepository gameLogMongoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private GameUnitOfWork unitOfWork;
    private TickLogSystem tickLogSystem;

    @BeforeEach
    void setUp() {
        this.unitOfWork = new GameUnitOfWork(
                this.gameClockMongoRepository,
                new GameLogService(this.gameLogMongoRepository)
        );
        this.tickLogSystem = new TickLogSystem(this.unitOfWork);
    }

    @Test
    void tickIfLiveAdvancesAndFlushesImmediately() {
        this.givenClock(GameClock.initial());
        ClockService clockService = this.service(this.eventPublisher, 1000);

        clockService.tickIfLive();

        GameClock saved = this.capturedClockSave();
        assertEquals(1, saved.tick());
        assertEquals(ClockMode.LIVE, saved.mode());
        assertFalse(saved.paused());
        verify(this.gameLogMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
        verify(this.eventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(TickAdvancedEvent.class));
    }

    @Test
    void tickIfLiveDoesNothingWhenPaused() {
        this.givenClock(GameClock.initial().pause());
        ClockService clockService = this.service(this.eventPublisher, 1000);

        clockService.tickIfLive();

        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void tickIfLiveDoesNothingInBatchMode() {
        this.givenClock(GameClock.initial().withMode(ClockMode.BATCH));
        ClockService clockService = this.service(this.eventPublisher, 1000);

        clockService.tickIfLive();

        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(this.eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void tickIfLiveFlushesLogEventsFromTickListenerImmediately() {
        this.givenClock(GameClock.initial());
        ClockService clockService = this.service(this.tickLogPublisher(), 1000);

        clockService.tickIfLive();

        List<GameLogDocument> events = this.capturedLogSave();
        assertEquals(1, events.size());
        assertEquals(1, events.getFirst().tick());
        assertEquals(GameLogType.TICK, events.getFirst().type());
        assertEquals("Tick 1", events.getFirst().detail());
    }

    @Test
    void advancePersistsClockAndLogOnceAtTheEnd() {
        this.givenClock(GameClock.initial());
        ClockService clockService = this.service(this.tickLogPublisher(), 1000);

        GameClock clock = clockService.advance(3);

        assertEquals(3, clock.tick());
        assertEquals(3, this.capturedClockSave().tick());
        List<GameLogDocument> events = this.capturedLogSave();
        assertEquals(3, events.size());
        assertEquals(1, events.getFirst().tick());
        assertEquals(3, events.get(2).tick());
        verify(this.gameClockMongoRepository, times(1)).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameLogMongoRepository, times(1)).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void advanceFlushesPeriodicallyDuringLongSimulation() {
        this.givenClock(GameClock.initial());
        ClockService clockService = this.service(this.tickLogPublisher(), 2);

        clockService.advance(5);

        verify(this.gameClockMongoRepository, times(3)).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameLogMongoRepository, times(3)).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void pauseFlushesImmediatelyAndPublishesEvent() {
        this.givenClock(new GameClock(12, ClockMode.LIVE, false));
        ClockService clockService = this.service(this.eventPublisher, 1000);

        GameClock paused = clockService.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
        assertTrue(this.capturedClockSave().paused());
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(this.eventPublisher).publishEvent(eventCaptor.capture());
        assertInstanceOf(ClockPausedEvent.class, eventCaptor.getValue());
    }

    private ClockService service(ApplicationEventPublisher publisher, int batchFlushEveryTicks) {
        return new ClockService(this.unitOfWork, List.of(), publisher, batchFlushEveryTicks);
    }

    private ApplicationEventPublisher tickLogPublisher() {
        return event -> {
            if (event instanceof TickAdvancedEvent tickAdvancedEvent) {
                this.tickLogSystem.onTickAdvanced(tickAdvancedEvent);
            }
        };
    }

    private void givenClock(GameClock clock) {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(clock)));
    }

    private GameClock capturedClockSave() {
        ArgumentCaptor<GameClockDocument> captor = ArgumentCaptor.forClass(GameClockDocument.class);
        verify(this.gameClockMongoRepository).save(captor.capture());
        return captor.getValue().toClock();
    }

    private List<GameLogDocument> capturedLogSave() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GameLogDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.gameLogMongoRepository).saveAll(captor.capture());
        return captor.getValue();
    }
}
