package com.example.rtnt.game.log.service;

import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.persistence.GameLogDocument;
import com.example.rtnt.game.log.persistence.GameLogMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameLogServiceTest {

    @Mock
    private GameLogMongoRepository gameLogMongoRepository;

    private GameLogService gameLogService;

    @BeforeEach
    void setUp() {
        this.gameLogService = new GameLogService(this.gameLogMongoRepository);
    }

    @Test
    void flushWritesBufferedEventsTogether() {
        this.gameLogService.append(new GameLogEvent(1, "BUY", "iron"));
        this.gameLogService.append(new GameLogEvent(2, "SELL", "iron"));
        this.gameLogService.flush();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GameLogDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.gameLogMongoRepository).saveAll(captor.capture());
        List<GameLogDocument> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertEquals("BUY", saved.get(0).type());
        assertEquals("SELL", saved.get(1).type());
        this.gameLogService.flush();
        verify(this.gameLogMongoRepository, times(1)).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void flushDoesNothingWhenEmpty() {
        this.gameLogService.flush();

        verify(this.gameLogMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
