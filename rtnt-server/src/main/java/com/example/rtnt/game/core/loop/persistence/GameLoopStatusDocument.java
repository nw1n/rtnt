package com.example.rtnt.game.core.loop.persistence;

import com.example.rtnt.game.core.clock.clock.domain.ClockMode;
import com.example.rtnt.game.core.clock.clock.domain.GameClock;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_loop_status")
@NullMarked
public record GameLoopStatusDocument(
        @Id String id,
        long tick,
        ClockMode mode,
        boolean paused
) {
    public static final String DOCUMENT_ID = "default";

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameLoopStatusDocument from(GameClock clock, ClockMode mode, boolean paused) {
        return new GameLoopStatusDocument(DOCUMENT_ID, clock.tick(), mode, paused);
    }
}
