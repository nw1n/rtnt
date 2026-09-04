package com.example.rtnt.game.trade.web;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeType;
import com.example.rtnt.game.trade.service.TradeEventStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/trade-events")
public class TradeEventController {
    private final TradeEventStore tradeEventStore;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public TradeEventController(TradeEventStore tradeEventStore) {
        this.tradeEventStore = tradeEventStore;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public List<TradeEventDto> getAll() {
        return this.tradeEventStore.list().stream()
                .map(TradeEventDto::from)
                .toList();
    }

    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record TradeEventDto(
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
        static TradeEventDto from(TradeEvent event) {
            return new TradeEventDto(
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
    }
}
