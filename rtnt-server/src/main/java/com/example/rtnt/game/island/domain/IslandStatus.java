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

    public IslandStatus consumeFoodOrStarve() {
        if (this.population < 1) {
            return this;
        }
        if (this.inventory.getAmount(GoodType.FOOD) >= 1) {
            return this.withInventory(this.inventory.removeAmount(GoodType.FOOD, 1));
        }
        long loss = (this.population + 9) / 10;
        return new IslandStatus(this.islandId, this.population - loss, this.inventory, this.tradePrices);
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
