package com.example.rtnt.domain.island;

import com.example.rtnt.domain.inventory.GoodType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class TradePriceList {
    private final EnumMap<GoodType, Integer> prices;

    private TradePriceList(Map<GoodType, Integer> prices) {
        this.prices = new EnumMap<>(prices);
    }

    public static TradePriceList defaultPrices() {
        return of(Map.of(
                GoodType.RUM, 1,
                GoodType.SUGAR, 1,
                GoodType.SPICES, 1,
                GoodType.TOBACCO, 1
        ));
    }

    public static TradePriceList of(Map<GoodType, Integer> prices) {
        Objects.requireNonNull(prices, "Trade prices cannot be null");

        EnumMap<GoodType, Integer> normalized = new EnumMap<>(GoodType.class);
        for (GoodType goodType : GoodType.tradeableGoods()) {
            Integer value = prices.get(goodType);
            if (value == null || value <= 0) {
                throw new IllegalArgumentException("Missing or invalid price for " + goodType);
            }
            normalized.put(goodType, value);
        }

        return new TradePriceList(normalized);
    }

    public int getPrice(GoodType goodType) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (!safeGoodType.isTradeable()) {
            throw new IllegalArgumentException("GOLD has no trade price");
        }
        Integer price = this.prices.get(safeGoodType);
        if (price == null) {
            throw new IllegalStateException("No price configured for " + safeGoodType);
        }
        return price;
    }

    public void setPrice(GoodType goodType, int price) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (!safeGoodType.isTradeable()) {
            throw new IllegalArgumentException("GOLD cannot have a trade price");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Trade price must be greater than 0");
        }
        this.prices.put(safeGoodType, price);
    }

    public Map<GoodType, Integer> getPrices() {
        return Collections.unmodifiableMap(this.prices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TradePriceList that = (TradePriceList) o;
        return Objects.equals(this.prices, that.prices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.prices);
    }

    @Override
    public String toString() {
        return "TradePriceList{prices=" + this.prices + "}";
    }
}
