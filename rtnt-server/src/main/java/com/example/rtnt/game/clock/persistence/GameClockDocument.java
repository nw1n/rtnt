package com.example.rtnt.game.clock.persistence;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_clock")
@NullMarked
public record GameClockDocument(
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

    public static GameClockDocument from(GameClock clock, ClockMode mode, boolean paused) {
        return new GameClockDocument(DOCUMENT_ID, clock.tick(), mode, paused);
    }
}
