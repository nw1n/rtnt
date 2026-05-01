package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.GoodType;
import java.time.Instant;

public record TradeEvent(
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
}
