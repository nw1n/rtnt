package com.example.rtnt.game.inventory.domain;

import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public class Inventory {
    private final EnumMap<GoodType, Integer> amounts;

    private Inventory(Map<GoodType, Integer> amounts) {
        this.amounts = new EnumMap<>(amounts);
    }

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static Inventory empty() {
        return of(Map.of());
    }

    public static Inventory of(Map<GoodType, Integer> amounts) {
        Objects.requireNonNull(amounts, "Inventory amounts cannot be null");
        EnumMap<GoodType, Integer> normalized = new EnumMap<>(GoodType.class);
        for (GoodType goodType : GoodType.values()) {
            int amount = amounts.getOrDefault(goodType, 0);
            if (amount < 0) {
                throw new IllegalArgumentException("Inventory amount cannot be negative for " + goodType);
            }
            normalized.put(goodType, amount);
        }
        return new Inventory(normalized);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public int getAmount(GoodType goodType) {
        return this.amounts.getOrDefault(Objects.requireNonNull(goodType, "Good type cannot be null"), 0);
    }

    public void setAmount(GoodType goodType, int amount) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (amount < 0) {
            throw new IllegalArgumentException("Inventory amount cannot be negative for " + safeGoodType);
        }
        this.amounts.put(safeGoodType, amount);
    }

    public void addAmount(GoodType goodType, int amountToAdd) {
        if (amountToAdd < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        this.setAmount(goodType, this.getAmount(goodType) + amountToAdd);
    }

    public void removeAmount(GoodType goodType, int amountToRemove) {
        if (amountToRemove < 0) {
            throw new IllegalArgumentException("Amount to remove cannot be negative");
        }
        int currentAmount = this.getAmount(goodType);
        if (amountToRemove > currentAmount) {
            throw new IllegalArgumentException("Cannot remove more than available inventory for " + goodType);
        }
        this.setAmount(goodType, currentAmount - amountToRemove);
    }

    public Map<GoodType, Integer> getAmounts() {
        return Collections.unmodifiableMap(this.amounts);
    }

    public int sumTradeableGoods() {
        int sum = 0;
        for (GoodType goodType : GoodType.tradeableGoods()) {
            sum += this.getAmount(goodType);
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Inventory inventory)) {
            return false;
        }
        return Objects.equals(this.amounts, inventory.amounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amounts);
    }

    @Override
    public String toString() {
        return "Inventory{amounts=" + this.amounts + "}";
    }
}
