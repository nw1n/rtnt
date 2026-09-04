package com.example.rtnt.game.trade.persistence;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "trade_events")
@NullMarked
public record TradeEventDocument(
        @Id String id,
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

    public static TradeEventDocument from(TradeEvent event) {
        return new TradeEventDocument(
                event.id(),
                event.tick(),
                event.timestamp(),
                event.tradeType(),
                event.shipId(),
                event.shipName(),
                event.islandId(),
                event.islandName(),
                event.goodType(),
                event.amount(),
                event.unitPrice(),
                event.totalPrice()
        );
    }

    public TradeEvent toTradeEvent() {
        return new TradeEvent(
                this.id,
                this.tick,
                this.timestamp,
                this.tradeType,
                this.shipId,
                this.shipName,
                this.islandId,
                this.islandName,
                this.goodType,
                this.amount,
                this.unitPrice,
                this.totalPrice
        );
    }
}
