package com.example.rtnt.game.inventory.domain;

import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public final class Inventory {
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

    public static Inventory islandSeed() {
        return of(Map.of(
                GoodType.GOLD, 100,
                GoodType.FOOD, 10,
                GoodType.RUM, 10,
                GoodType.SUGAR, 10,
                GoodType.SPICES, 10,
                GoodType.TOBACCO, 10
        ));
    }

    public static Inventory shipSeed() {
        return of(Map.of(
                GoodType.GOLD, 1_000,
                GoodType.FOOD, 4,
                GoodType.RUM, 5,
                GoodType.SUGAR, 4,
                GoodType.SPICES, 3,
                GoodType.TOBACCO, 6
        ));
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

    public Inventory withAmount(GoodType goodType, int amount) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (amount < 0) {
            throw new IllegalArgumentException("Inventory amount cannot be negative for " + safeGoodType);
        }
        EnumMap<GoodType, Integer> next = new EnumMap<>(this.amounts);
        next.put(safeGoodType, amount);
        return new Inventory(next);
    }

    public Inventory addAmount(GoodType goodType, int amountToAdd) {
        if (amountToAdd < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        return this.withAmount(goodType, this.getAmount(goodType) + amountToAdd);
    }

    public Inventory removeAmount(GoodType goodType, int amountToRemove) {
        GoodType safeGoodType = Objects.requireNonNull(goodType, "Good type cannot be null");
        if (amountToRemove < 0) {
            throw new IllegalArgumentException("Amount to remove cannot be negative");
        }
        int currentAmount = this.getAmount(safeGoodType);
        if (amountToRemove > currentAmount) {
            throw new IllegalArgumentException("Cannot remove more than available inventory for " + safeGoodType);
        }
        return this.withAmount(safeGoodType, currentAmount - amountToRemove);
    }

    public Map<GoodType, Integer> amounts() {
        return Collections.unmodifiableMap(this.amounts);
    }

    /** Sum of tradeable goods (food, rum, sugar, spices, tobacco). Gold is not hold space. */
    public int sumTradeableGoods() {
        int sum = 0;
        for (GoodType goodType : GoodType.tradeableGoods()) {
            sum += this.getAmount(goodType);
        }
        return sum;
    }

    public boolean allTradeableAtLeast(int threshold) {
        if (threshold < 1) {
            throw new IllegalArgumentException("threshold must be at least 1");
        }
        for (GoodType goodType : GoodType.tradeableGoods()) {
            if (this.getAmount(goodType) < threshold) {
                return false;
            }
        }
        return true;
    }

    /** Keeps half of each tradeable good (floored). Gold is unchanged. */
    public Inventory consumeHalfTradeableGoods() {
        EnumMap<GoodType, Integer> next = new EnumMap<>(this.amounts);
        for (GoodType goodType : GoodType.tradeableGoods()) {
            next.put(goodType, this.getAmount(goodType) / 2);
        }
        return new Inventory(next);
    }

    /** Halves each tradeable good that is strictly above the threshold. Gold is unchanged. */
    public Inventory spoilOverstockedTradeableGoods(int threshold) {
        if (threshold < 1) {
            throw new IllegalArgumentException("threshold must be at least 1");
        }
        EnumMap<GoodType, Integer> next = new EnumMap<>(this.amounts);
        boolean changed = false;
        for (GoodType goodType : GoodType.tradeableGoods()) {
            int amount = this.getAmount(goodType);
            if (amount > threshold) {
                next.put(goodType, amount / 2);
                changed = true;
            }
        }
        return changed ? new Inventory(next) : this;
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
