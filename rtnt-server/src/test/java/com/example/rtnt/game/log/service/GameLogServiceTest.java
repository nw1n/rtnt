package com.example.rtnt.game.log.service;

import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.domain.GameLogType;
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
import static org.mockito.Mockito.when;

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
        this.gameLogService.append(new GameLogEvent(1, GameLogType.TICK, "Tick 1"));
        this.gameLogService.append(new GameLogEvent(2, GameLogType.TICK, "Tick 2"));
        this.gameLogService.flush();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GameLogDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.gameLogMongoRepository).saveAll(captor.capture());
        List<GameLogDocument> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertEquals(GameLogType.TICK, saved.get(0).type());
        assertEquals(GameLogType.TICK, saved.get(1).type());
        this.gameLogService.flush();
        verify(this.gameLogMongoRepository, times(1)).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void flushDoesNothingWhenEmpty() {
        this.gameLogService.flush();

        verify(this.gameLogMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listRecentReadsPersistedEvents() {
        when(this.gameLogMongoRepository.findTop200ByOrderByTickDesc())
                .thenReturn(List.of(GameLogDocument.from(GameLogEvent.forTick(2))));

        List<GameLogEvent> events = this.gameLogService.listRecent();

        assertEquals(1, events.size());
        assertEquals(2, events.getFirst().tick());
        assertEquals("Tick 2", events.getFirst().detail());
    }
}
