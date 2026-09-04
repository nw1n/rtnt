package com.example.rtnt.game.trade.service;

import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.persistence.TradeEventDocument;
import com.example.rtnt.game.trade.persistence.TradeEventMongoRepository;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NullMarked
public class TradeEventStore {
    private static final Logger log = LoggerFactory.getLogger(TradeEventStore.class);

    private final TradeEventMongoRepository tradeEventMongoRepository;
    private final boolean persistEvents;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public TradeEventStore(
            TradeEventMongoRepository tradeEventMongoRepository,
            @Value("${rtnt.trade.persist-events:true}") boolean persistEvents
    ) {
        this.tradeEventMongoRepository = tradeEventMongoRepository;
        this.persistEvents = persistEvents;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public void record(List<TradeEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        for (TradeEvent event : events) {
            log.info(
                    "Trade {} {} x{} @ {} gold (total {}) ship={} island={} tick={}",
                    event.tradeType(),
                    event.goodType(),
                    event.amount(),
                    event.unitPrice(),
                    event.totalPrice(),
                    event.shipName(),
                    event.islandName(),
                    event.tick()
            );
        }
        if (this.persistEvents) {
            this.tradeEventMongoRepository.saveAll(events.stream().map(TradeEventDocument::from).toList());
        }
    }

    public List<TradeEvent> list() {
        if (!this.persistEvents) {
            return List.of();
        }
        return this.tradeEventMongoRepository.findAll().stream()
                .map(TradeEventDocument::toTradeEvent)
                .toList();
    }

    public boolean persistEvents() {
        return this.persistEvents;
    }
}
