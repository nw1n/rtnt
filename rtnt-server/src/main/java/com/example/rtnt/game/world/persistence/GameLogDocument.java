package com.example.rtnt.game.world.persistence;

import com.example.rtnt.game.world.GameLogEvent;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "game_log")
@NullMarked
public record GameLogDocument(
        @Id String id,
        long tick,
        String type,
        String detail
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameLogDocument from(GameLogEvent event) {
        return new GameLogDocument(UUID.randomUUID().toString(), event.tick(), event.type(), event.detail());
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public GameLogEvent toEvent() {
        return new GameLogEvent(this.tick, this.type, this.detail);
    }
}
