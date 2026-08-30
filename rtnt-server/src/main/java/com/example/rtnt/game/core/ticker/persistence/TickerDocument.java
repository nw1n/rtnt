package com.example.rtnt.game.core.ticker.persistence;

import com.example.rtnt.game.core.ticker.GameTick;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ticker")
@NullMarked
public record TickerDocument(
        @Id String id,
        long tick
) {
    public static final String DOCUMENT_ID = "default";

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static TickerDocument from(GameTick gameTick) {
        return new TickerDocument(DOCUMENT_ID, gameTick.tick());
    }
}
