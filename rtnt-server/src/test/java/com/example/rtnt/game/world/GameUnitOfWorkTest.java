package com.example.rtnt.game.world;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.persistence.GameClockDocument;
import com.example.rtnt.game.clock.persistence.GameClockMongoRepository;
import com.example.rtnt.game.world.persistence.GameLogDocument;
import com.example.rtnt.game.world.persistence.GameLogMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private GameLogMongoRepository gameLogMongoRepository;

    private GameUnitOfWork unitOfWork;

    @BeforeEach
    void setUp() {
        this.unitOfWork = new GameUnitOfWork(this.gameClockMongoRepository, this.gameLogMongoRepository);
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
    }

    @Test
    void flushWritesBufferedLogEventsTogether() {
        this.unitOfWork.append(new GameLogEvent(1, "BUY", "iron"));
        this.unitOfWork.append(new GameLogEvent(2, "SELL", "iron"));
        this.unitOfWork.flush();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GameLogDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.gameLogMongoRepository).saveAll(captor.capture());
        List<GameLogDocument> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertEquals("BUY", saved.get(0).type());
        assertEquals("SELL", saved.get(1).type());
        this.unitOfWork.flush();
        verify(this.gameLogMongoRepository, times(1)).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
