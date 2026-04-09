package com.example.rtnt.domain.inventory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Inventory {
    private final EnumMap<GoodType, Integer> amounts;

    private Inventory(Map<GoodType, Integer> amounts) {
        this.amounts = new EnumMap<>(amounts);
    }

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

    public int getAmount(GoodType goodType) {
        return this.amounts.getOrDefault(
                Objects.requireNonNull(goodType, "Good type cannot be null"),
                0
        );
    }

    public void setAmount(GoodType goodType, int amount) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (amount < 0) {
            throw new IllegalArgumentException("Inventory amount cannot be negative for " + safeGoodType);
        }
        this.amounts.put(safeGoodType, amount);
    }

    public void addAmount(GoodType goodType, int amountToAdd) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (amountToAdd < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        this.setAmount(safeGoodType, this.getAmount(safeGoodType) + amountToAdd);
    }

    public void removeAmount(GoodType goodType, int amountToRemove) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (amountToRemove < 0) {
            throw new IllegalArgumentException("Amount to remove cannot be negative");
        }

        int currentAmount = this.getAmount(safeGoodType);
        if (amountToRemove > currentAmount) {
            throw new IllegalArgumentException("Cannot remove more than available inventory for " + safeGoodType);
        }
        this.setAmount(safeGoodType, currentAmount - amountToRemove);
    }

    public Map<GoodType, Integer> getAmounts() {
        return Collections.unmodifiableMap(this.amounts);
    }

    /** Sum of tradeable goods (rum, sugar, spices, tobacco). Gold is not hold space. */
    public int sumTradeableGoods() {
        int sum = 0;
        for (GoodType goodType : GoodType.tradeableGoods()) {
            sum += this.getAmount(goodType);
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
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
