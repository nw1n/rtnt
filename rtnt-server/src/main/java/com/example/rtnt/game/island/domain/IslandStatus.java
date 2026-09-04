package com.example.rtnt.game.island.domain;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record IslandStatus(
        String islandId,
        long population,
        Inventory inventory,
        TradePriceList tradePrices
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static IslandStatus initial(String islandId) {
        return new IslandStatus(islandId, 1_000, Inventory.islandSeed(), TradePriceList.islandSeed());
    }

    public static IslandStatus empty(String islandId) {
        return new IslandStatus(islandId, 0, Inventory.empty(), TradePriceList.defaultPrices());
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public IslandStatus grow(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("growth amount must be at least 1");
        }
        return new IslandStatus(this.islandId, this.population + amount, this.inventory, this.tradePrices);
    }

    public IslandStatus produce(GoodType goodType, int amount) {
        if (!goodType.isTradeable()) {
            throw new IllegalArgumentException("islands only produce tradeable goods");
        }
        return this.withInventory(this.inventory.addAmount(goodType, amount));
    }

    public boolean canFlourish(int goodsThreshold) {
        return this.inventory.allTradeableAtLeast(goodsThreshold);
    }

    public IslandStatus consumeHalfGoodsAndGrow(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("growth amount must be at least 1");
        }
        return new IslandStatus(
                this.islandId,
                this.population + amount,
                this.inventory.consumeHalfTradeableGoods(),
                this.tradePrices
        );
    }

    public IslandStatus spoilOverstockedGoods(int threshold) {
        return this.withInventory(this.inventory.spoilOverstockedTradeableGoods(threshold));
    }

    public static int foodNeeded(long population) {
        if (population < 1) {
            return 0;
        }
        return (int) ((population + 249) / 250);
    }

    public IslandStatus consumeFoodOrStarve() {
        if (this.population < 1) {
            return this;
        }
        int needed = foodNeeded(this.population);
        int available = this.inventory.getAmount(GoodType.FOOD);
        if (available >= needed) {
            return this.withInventory(this.inventory.removeAmount(GoodType.FOOD, needed));
        }
        Inventory remaining = available > 0
                ? this.inventory.removeAmount(GoodType.FOOD, available)
                : this.inventory;
        long loss = (this.population + 9) / 10;
        return new IslandStatus(this.islandId, this.population - loss, remaining, this.tradePrices);
    }

    public IslandStatus adjustPrices(int needThreshold, int surplusThreshold, int minPrice, int maxPrice) {
        return this.withTradePrices(
                this.tradePrices.adjustForSupply(this.inventory, needThreshold, surplusThreshold, minPrice, maxPrice)
        );
    }

    public IslandStatus withInventory(Inventory inventory) {
        return new IslandStatus(this.islandId, this.population, inventory, this.tradePrices);
    }

    public IslandStatus withTradePrices(TradePriceList tradePrices) {
        return new IslandStatus(this.islandId, this.population, this.inventory, tradePrices);
    }
}
