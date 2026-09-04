package com.example.rtnt.game.core.worldsnapshot;

import com.example.rtnt.game.core.loop.GameLoop;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class TickZeroWorldSnapshot implements ApplicationRunner {
    private final GameLoop gameLoop;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public TickZeroWorldSnapshot(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public void run(@NonNull ApplicationArguments args) {
        this.gameLoop.snapshotIfAtTickZero();
    }
}
