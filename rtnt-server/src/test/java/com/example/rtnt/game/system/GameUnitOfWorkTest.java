package com.example.rtnt.game.system;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.service.GameLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameUnitOfWorkTest {

    @Mock
    private GameClockMongoRepository gameClockMongoRepository;

    @Mock
    private GameLogService gameLogService;

    private GameUnitOfWork unitOfWork;

    @BeforeEach
    void setUp() {
        this.unitOfWork = new GameUnitOfWork(this.gameClockMongoRepository, this.gameLogService);
    }

    @Test
    void clockLoadsExistingDocument() {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(new GameClock(7, ClockMode.LIVE, true))));

        GameClock clock = this.unitOfWork.clock();

        assertEquals(7, clock.tick());
        verify(this.gameClockMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void clockSeedsWhenMissing() {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID)).thenReturn(Optional.empty());

        GameClock clock = this.unitOfWork.clock();

        assertEquals(0, clock.tick());
        verify(this.gameClockMongoRepository).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameLogService).flush();
    }

    @Test
    void flushWritesLatestClockOnce() {
        when(this.gameClockMongoRepository.findById(GameClockDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(GameClockDocument.from(GameClock.initial())));

        this.unitOfWork.replaceClock(this.unitOfWork.clock().advance());
        this.unitOfWork.replaceClock(this.unitOfWork.clock().advance());
        this.unitOfWork.replaceClock(this.unitOfWork.clock().advance());
        this.unitOfWork.flush();

        ArgumentCaptor<GameClockDocument> captor = ArgumentCaptor.forClass(GameClockDocument.class);
        verify(this.gameClockMongoRepository).save(captor.capture());
        assertEquals(3, captor.getValue().toClock().tick());
        this.unitOfWork.flush();
        verify(this.gameClockMongoRepository, times(1)).save(org.mockito.ArgumentMatchers.any());
        verify(this.gameLogService, times(2)).flush();
    }

    @Test
    void appendDelegatesToLogFeature() {
        GameLogEvent event = new GameLogEvent(1, "BUY", "iron");

        this.unitOfWork.append(event);

        verify(this.gameLogService).append(event);
    }
}
