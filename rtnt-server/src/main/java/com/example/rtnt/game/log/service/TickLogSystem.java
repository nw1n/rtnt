package com.example.rtnt.game.log.service;

import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.system.GameSystem;
import com.example.rtnt.game.system.GameUnitOfWork;
import org.springframework.stereotype.Component;

@Component
public class TickLogSystem implements GameSystem {
    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public void onTick(GameClock clock, GameUnitOfWork unitOfWork) {
        unitOfWork.append(GameLogEvent.forTick(clock.tick()));
    }
}
