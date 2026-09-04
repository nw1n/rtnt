package com.example.rtnt.game.trade.domain;

import com.example.rtnt.game.inventory.domain.GoodType;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.UUID;

@NullMarked
public record TradeEvent(
        String id,
        long tick,
        Instant timestamp,
        TradeType tradeType,
        String shipId,
        String shipName,
        String islandId,
        String islandName,
        GoodType goodType,
        int amount,
        int unitPrice,
        int totalPrice
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static TradeEvent create(
            long tick,
            TradeType tradeType,
            String shipId,
            String shipName,
            String islandId,
            String islandName,
            GoodType goodType,
            int amount,
            int unitPrice,
            int totalPrice
    ) {
        return new TradeEvent(
                UUID.randomUUID().toString(),
                tick,
                Instant.now(),
                tradeType,
                shipId,
                shipName,
                islandId,
                islandName,
                goodType,
                amount,
                unitPrice,
                totalPrice
        );
    }
}
