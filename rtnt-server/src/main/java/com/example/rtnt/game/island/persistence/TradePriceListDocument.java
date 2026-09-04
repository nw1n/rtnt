package com.example.rtnt.game.island.persistence;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.island.domain.TradePriceList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@NullMarked
public record TradePriceListDocument(@Nullable Map<GoodType, Integer> prices) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static TradePriceListDocument from(TradePriceList tradePrices) {
        return new TradePriceListDocument(new EnumMap<>(tradePrices.prices()));
    }

    public static TradePriceList toTradePriceList(@Nullable TradePriceListDocument document) {
        if (document == null || document.prices() == null || document.prices().isEmpty()) {
            return TradePriceList.defaultPrices();
        }
        return TradePriceList.of(document.prices());
    }
}
