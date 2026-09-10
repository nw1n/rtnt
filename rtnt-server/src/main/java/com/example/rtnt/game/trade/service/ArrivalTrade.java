package com.example.rtnt.game.trade.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.domain.TradePriceList;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeResult;
import com.example.rtnt.game.trade.domain.TradeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@NullMarked
public class ArrivalTrade {
    private static final TradePriceList FAIR_PRICES = TradePriceList.islandSeed();
    private static final double MIN_SELL_CHANCE = 0.2;
    private static final double MAX_SELL_CHANCE = 0.8;
    private static final int BUY_TAX_PERCENT = 5;

    private final Random random;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    @Autowired
    public ArrivalTrade() {
        this(new Random());
    }

    ArrivalTrade(Random random) {
        this.random = random;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public TradeResult execute(Ship ship, Island island, IslandStatus status, long tick) {
        Inventory shipInventory = ship.getInventory();
        Inventory islandInventory = status.inventory();
        List<TradeEvent> events = new ArrayList<>();
        List<GoodType> goods = new ArrayList<>(GoodType.tradeableGoods());
        this.shuffle(goods);
        int remainingGoods = 1 + this.random.nextInt(goods.size());
        for (GoodType good : goods) {
            if (remainingGoods == 0) {
                break;
            }
            int unitPrice = status.tradePrices().getPrice(good);
            int maxBuy = maxBuyAmount(ship.getCargoCapacity(), shipInventory, islandInventory, good, unitPrice);
            int maxSell = maxSellAmount(shipInventory, islandInventory, good, unitPrice);
            boolean canBuy = maxBuy >= 1;
            boolean canSell = maxSell >= 1;
            if (!canBuy && !canSell) {
                continue;
            }
            boolean sell = canSell && (!canBuy || this.prefersSell(unitPrice, good));
            int amount = 1 + this.random.nextInt(sell ? maxSell : maxBuy);
            AppliedTrade applied = apply(
                    ship,
                    island,
                    shipInventory,
                    islandInventory,
                    tick,
                    sell ? TradeType.SELL_TO_ISLAND : TradeType.BUY_FROM_ISLAND,
                    good,
                    amount,
                    unitPrice
            );
            shipInventory = applied.shipInventory();
            islandInventory = applied.islandInventory();
            events.add(applied.event());
            remainingGoods--;
        }
        return new TradeResult(
                ship.withInventory(shipInventory),
                status.withInventory(islandInventory),
                List.copyOf(events)
        );
    }

    /***************************************************************************
     *                                                                         *
     * Package helpers                                                         *
     *                                                                         *
     **************************************************************************/

    static int maxBuyAmount(
            int cargoCapacity,
            Inventory shipInventory,
            Inventory islandInventory,
            GoodType good,
            int unitPrice
    ) {
        if (unitPrice <= 0) {
            return 0;
        }
        int holdRoom = cargoCapacity - shipInventory.sumTradeableGoods();
        int islandStock = islandInventory.getAmount(good);
        int unitCost = unitPrice + buyTax(unitPrice);
        int maxByGold = shipInventory.getAmount(GoodType.GOLD) / unitCost;
        return Math.min(holdRoom, Math.min(islandStock, maxByGold));
    }

    static int buyTax(int unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unit price cannot be negative");
        }
        return Math.multiplyExact(unitPrice, BUY_TAX_PERCENT) / 100;
    }

    static double sellProbability(int unitPrice, int fairPrice) {
        int safeFair = Math.max(fairPrice, 1);
        double ratio = unitPrice / (double) safeFair;
        double probability = 0.5 + 0.25 * (ratio - 1.0);
        return Math.min(MAX_SELL_CHANCE, Math.max(MIN_SELL_CHANCE, probability));
    }

    static int maxSellAmount(Inventory shipInventory, Inventory islandInventory, GoodType good, int unitPrice) {
        if (unitPrice <= 0) {
            return 0;
        }
        int shipStock = shipInventory.getAmount(good);
        int maxByIslandGold = islandInventory.getAmount(GoodType.GOLD) / unitPrice;
        return Math.min(shipStock, maxByIslandGold);
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private boolean prefersSell(int unitPrice, GoodType good) {
        return this.random.nextDouble() < sellProbability(unitPrice, FAIR_PRICES.getPrice(good));
    }

    private void shuffle(List<GoodType> goods) {
        for (int i = goods.size() - 1; i > 0; i--) {
            int j = this.random.nextInt(i + 1);
            GoodType tmp = goods.get(i);
            goods.set(i, goods.get(j));
            goods.set(j, tmp);
        }
    }

    private record AppliedTrade(Inventory shipInventory, Inventory islandInventory, TradeEvent event) {
    }

    private static AppliedTrade apply(
            Ship ship,
            Island island,
            Inventory shipInventory,
            Inventory islandInventory,
            long tick,
            TradeType tradeType,
            GoodType good,
            int amount,
            int unitPrice
    ) {
        int goodsCost = Math.multiplyExact(unitPrice, amount);
        int tax = 0;
        Inventory nextShip;
        Inventory nextIsland;
        if (tradeType == TradeType.SELL_TO_ISLAND) {
            nextShip = shipInventory.removeAmount(good, amount).addAmount(GoodType.GOLD, goodsCost);
            nextIsland = islandInventory.addAmount(good, amount).removeAmount(GoodType.GOLD, goodsCost);
        } else {
            tax = Math.multiplyExact(buyTax(unitPrice), amount);
            int shipPayment = goodsCost + tax;
            nextShip = shipInventory.removeAmount(GoodType.GOLD, shipPayment).addAmount(good, amount);
            nextIsland = islandInventory.addAmount(GoodType.GOLD, shipPayment).removeAmount(good, amount);
        }
        return new AppliedTrade(
                nextShip,
                nextIsland,
                TradeEvent.create(
                        tick,
                        tradeType,
                        ship.getId(),
                        ship.getName(),
                        island.id(),
                        island.name(),
                        good,
                        amount,
                        unitPrice,
                        goodsCost + tax
                )
        );
    }
}
