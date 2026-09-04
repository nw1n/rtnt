package com.example.rtnt.game.island.domain;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record Island(
    String id,
    String name,
    Footprint footprint,
    TradePriceList tradePrices
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static Island create(String name, Footprint footprint) {
        return new Island(UUID.randomUUID().toString(), name, footprint, TradePriceList.islandSeed());
    }

    public static Island existing(String id, String name, Footprint footprint) {
        return existing(id, name, footprint, TradePriceList.defaultPrices());
    }

    public static Island existing(String id, String name, Footprint footprint, TradePriceList tradePrices) {
        return new Island(id, name, footprint, tradePrices);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Island withTradePrices(TradePriceList tradePrices) {
        return new Island(this.id, this.name, this.footprint, tradePrices);
    }
}
