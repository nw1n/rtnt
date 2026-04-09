package com.example.rtnt.usecase.game;

import com.example.rtnt.adapter.out.persistence.game.GameLoopStateDocument;
import com.example.rtnt.adapter.out.persistence.game.GameLoopStateMongoRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameLoopServiceTest {

    @Test
    void initializesFromPersistedTickWhenStateExists() {
        GameLoopStateMongoRepository repository = mock(GameLoopStateMongoRepository.class);
        when(repository.findById("game-loop"))
                .thenReturn(Optional.of(new GameLoopStateDocument("game-loop", 42L)));

        GameLoopService gameLoopService = new GameLoopService(repository);

        assertEquals(42L, gameLoopService.getCurrentTick());
    }

    @Test
    void startsAtZeroWhenNoPersistedStateExists() {
        GameLoopStateMongoRepository repository = mock(GameLoopStateMongoRepository.class);
        when(repository.findById("game-loop")).thenReturn(Optional.empty());

        GameLoopService gameLoopService = new GameLoopService(repository);

        assertEquals(0L, gameLoopService.getCurrentTick());
    }

    @Test
    void tickAndScheduledTickIncrementSameCounter() {
        GameLoopStateMongoRepository repository = mock(GameLoopStateMongoRepository.class);
        when(repository.findById("game-loop")).thenReturn(Optional.empty());

        GameLoopService gameLoopService = new GameLoopService(repository);

        assertEquals(1L, gameLoopService.tick());
        gameLoopService.scheduledTick();

        assertEquals(2L, gameLoopService.getCurrentTick());
        verify(repository, times(1)).findById("game-loop");
    }

    @Test
    void scheduledPersistAndShutdownPersistCurrentTick() {
        GameLoopStateMongoRepository repository = mock(GameLoopStateMongoRepository.class);
        when(repository.findById("game-loop")).thenReturn(Optional.empty());

        GameLoopService gameLoopService = new GameLoopService(repository);
        gameLoopService.tick();
        gameLoopService.tick();

        gameLoopService.scheduledPersist();
        gameLoopService.persistOnShutdown();

        verify(repository, times(2)).save(argThat(document ->
                "game-loop".equals(document.getId()) && document.getCurrentTick() == 2L));
    }
}
