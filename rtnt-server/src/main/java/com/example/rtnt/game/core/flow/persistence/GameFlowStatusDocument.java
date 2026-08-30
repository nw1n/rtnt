package com.example.rtnt.game.core.flow.persistence;

import com.example.rtnt.game.core.flow.GameTick;
import com.example.rtnt.game.core.flow.TimeMode;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_flow_status")
@NullMarked
public record GameFlowStatusDocument(
        @Id String id,
        long tick,
        TimeMode mode,
        boolean paused
) {
    public static final String DOCUMENT_ID = "default";

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameFlowStatusDocument from(GameTick gameTick, TimeMode mode, boolean paused) {
        return new GameFlowStatusDocument(DOCUMENT_ID, gameTick.tick(), mode, paused);
    }
}
