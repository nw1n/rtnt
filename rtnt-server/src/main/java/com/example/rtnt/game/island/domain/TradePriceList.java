package com.example.rtnt.game.island.domain;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public final class TradePriceList {
    private final EnumMap<GoodType, Integer> prices;

    private TradePriceList(Map<GoodType, Integer> prices) {
        this.prices = new EnumMap<>(prices);
    }

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static TradePriceList defaultPrices() {
        return of(Map.of(
                GoodType.FOOD, 1,
                GoodType.RUM, 1,
                GoodType.SUGAR, 1,
                GoodType.SPICES, 1,
                GoodType.TOBACCO, 1
        ));
    }

    public static TradePriceList islandSeed() {
        return of(Map.of(
                GoodType.FOOD, 2,
                GoodType.RUM, 3,
                GoodType.SUGAR, 2,
                GoodType.SPICES, 4,
                GoodType.TOBACCO, 5
        ));
    }

    public static TradePriceList of(Map<GoodType, Integer> prices) {
        Objects.requireNonNull(prices, "Trade prices cannot be null");
        EnumMap<GoodType, Integer> normalized = new EnumMap<>(GoodType.class);
        for (GoodType goodType : GoodType.tradeableGoods()) {
            int value = prices.getOrDefault(goodType, 1);
            if (value <= 0) {
                throw new IllegalArgumentException("Missing or invalid price for " + goodType);
            }
            normalized.put(goodType, value);
        }
        return new TradePriceList(normalized);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

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

    public TradePriceList adjustForSupply(
            Inventory inventory,
            int needThreshold,
            int surplusThreshold,
            int minPrice,
            int maxPrice
    ) {
        if (needThreshold < 0) {
            throw new IllegalArgumentException("needThreshold must be at least 0");
        }
        if (surplusThreshold <= needThreshold) {
            throw new IllegalArgumentException("surplusThreshold must be greater than needThreshold");
        }
        if (minPrice < 1) {
            throw new IllegalArgumentException("minPrice must be at least 1");
        }
        if (maxPrice < minPrice) {
            throw new IllegalArgumentException("maxPrice must be >= minPrice");
        }
        EnumMap<GoodType, Integer> next = new EnumMap<>(this.prices);
        boolean changed = false;
        for (GoodType goodType : GoodType.tradeableGoods()) {
            int stock = inventory.getAmount(goodType);
            int price = this.getPrice(goodType);
            int adjusted = price;
            if (stock < needThreshold) {
                adjusted = Math.min(maxPrice, price + 1);
            } else if (stock > surplusThreshold) {
                adjusted = Math.max(minPrice, price - 1);
            }
            if (adjusted != price) {
                next.put(goodType, adjusted);
                changed = true;
            }
        }
        return changed ? new TradePriceList(next) : this;
    }

    public TradePriceList withPrice(GoodType goodType, int price) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (!safeGoodType.isTradeable()) {
            throw new IllegalArgumentException("GOLD cannot have a trade price");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Trade price must be greater than 0");
        }
        EnumMap<GoodType, Integer> next = new EnumMap<>(this.prices);
        next.put(safeGoodType, price);
        return new TradePriceList(next);
    }

    public Map<GoodType, Integer> prices() {
        return Collections.unmodifiableMap(this.prices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TradePriceList that)) {
            return false;
        }
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
