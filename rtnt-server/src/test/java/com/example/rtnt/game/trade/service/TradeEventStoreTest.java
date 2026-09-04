package com.example.rtnt.game.trade.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeType;
import com.example.rtnt.game.trade.persistence.TradeEventDocument;
import com.example.rtnt.game.trade.persistence.TradeEventMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeEventStoreTest {

    @Mock
    private TradeEventMongoRepository tradeEventMongoRepository;

    @Test
    void recordPersistsWhenEnabled() {
        TradeEventStore store = new TradeEventStore(this.tradeEventMongoRepository, true);
        TradeEvent event = TradeEvent.create(
                10,
                TradeType.BUY_FROM_ISLAND,
                "s1",
                "Pearl",
                "i1",
                "Jamaica",
                GoodType.RUM,
                2,
                3,
                6
        );

        store.record(List.of(event));

        verify(this.tradeEventMongoRepository).saveAll(List.of(TradeEventDocument.from(event)));
    }

    @Test
    void recordSkipsMongoWhenDisabled() {
        TradeEventStore store = new TradeEventStore(this.tradeEventMongoRepository, false);
        TradeEvent event = TradeEvent.create(
                10,
                TradeType.BUY_FROM_ISLAND,
                "s1",
                "Pearl",
                "i1",
                "Jamaica",
                GoodType.RUM,
                2,
                3,
                6
        );

        store.record(List.of(event));

        verify(this.tradeEventMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
