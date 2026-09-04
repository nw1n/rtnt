package com.example.rtnt.game.island.web;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.island.domain.TradePriceList;

public record TradePricesDto(int food, int rum, int sugar, int spices, int tobacco) {
    public static TradePricesDto from(TradePriceList tradePrices) {
        return new TradePricesDto(
                tradePrices.getPrice(GoodType.FOOD),
                tradePrices.getPrice(GoodType.RUM),
                tradePrices.getPrice(GoodType.SUGAR),
                tradePrices.getPrice(GoodType.SPICES),
                tradePrices.getPrice(GoodType.TOBACCO)
        );
    }
}
