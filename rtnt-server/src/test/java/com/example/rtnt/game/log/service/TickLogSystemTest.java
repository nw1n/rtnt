package com.example.rtnt.game.log.service;

import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.event.TickAdvancedEvent;
import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.system.GameUnitOfWork;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TickLogSystemTest {

    @Mock
    private GameUnitOfWork unitOfWork;

    @Test
    void onTickAdvancedAppendsTickMessage() {
        new TickLogSystem(this.unitOfWork)
                .onTickAdvanced(new TickAdvancedEvent(GameClock.initial().advance()));

        verify(this.unitOfWork).append(GameLogEvent.forTick(1));
    }
}
