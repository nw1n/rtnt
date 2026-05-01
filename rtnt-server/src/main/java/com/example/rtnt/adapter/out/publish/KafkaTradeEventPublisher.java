package com.example.rtnt.adapter.out.publish;

import com.example.rtnt.usecase.ship.TradeEvent;
import com.example.rtnt.usecase.ship.TradeEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class KafkaTradeEventPublisher implements TradeEventPublisher {
    private final Producer producer;

    public KafkaTradeEventPublisher(Producer producer) {
        this.producer = producer;
    }

    @Override
    public void publish(TradeEvent tradeEvent) {
        this.producer.produceTradeEvent(tradeEvent);
    }
}
