package com.example.rtnt.game.system;

import com.example.rtnt.game.clock.domain.GameClock;

public interface GameSystem {
    void onTick(GameClock clock, GameUnitOfWork unitOfWork);
}
