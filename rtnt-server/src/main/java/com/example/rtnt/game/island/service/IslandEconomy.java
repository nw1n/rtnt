package com.example.rtnt.game.island.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.persistence.IslandStatusDocument;
import com.example.rtnt.game.island.persistence.IslandStatusMongoRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@NullMarked
public class IslandEconomy {
    private static final int FOOD_PRODUCTION_WEIGHT = 3;

    private final IslandStatusMongoRepository islandStatusMongoRepository;
    private final int intervalTicks;
    private final int foodIntervalTicks;
    private final double productionChance;
    private final int productionMin;
    private final int productionMax;
    private final int growthGoodsThreshold;
    private final int spoilageThreshold;
    private final int priceNeedThreshold;
    private final int priceSurplusThreshold;
    private final int priceMin;
    private final int priceMax;
    private final int growthPercent;
    private final Random random;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    @Autowired
    public IslandEconomy(
            IslandStatusMongoRepository islandStatusMongoRepository,
            @Value("${rtnt.island.economy-interval-ticks:100}") int intervalTicks,
            @Value("${rtnt.island.food-interval-ticks:500}") int foodIntervalTicks,
            @Value("${rtnt.island.production-chance:0.45}") double productionChance,
            @Value("${rtnt.island.production-min:2}") int productionMin,
            @Value("${rtnt.island.production-max:8}") int productionMax,
            @Value("${rtnt.island.growth-goods-threshold:15}") int growthGoodsThreshold,
            @Value("${rtnt.island.spoilage-threshold:40}") int spoilageThreshold,
            @Value("${rtnt.island.price-need-threshold:10}") int priceNeedThreshold,
            @Value("${rtnt.island.price-surplus-threshold:30}") int priceSurplusThreshold,
            @Value("${rtnt.island.price-min:1}") int priceMin,
            @Value("${rtnt.island.price-max:20}") int priceMax,
            @Value("${rtnt.island.population-growth-percent:5}") int growthPercent
    ) {
        this(
                islandStatusMongoRepository,
                intervalTicks,
                foodIntervalTicks,
                productionChance,
                productionMin,
                productionMax,
                growthGoodsThreshold,
                spoilageThreshold,
                priceNeedThreshold,
                priceSurplusThreshold,
                priceMin,
                priceMax,
                growthPercent,
                new Random()
        );
    }

    IslandEconomy(
            IslandStatusMongoRepository islandStatusMongoRepository,
            int intervalTicks,
            int foodIntervalTicks,
            double productionChance,
            int productionMin,
            int productionMax,
            int growthGoodsThreshold,
            int spoilageThreshold,
            int priceNeedThreshold,
            int priceSurplusThreshold,
            int priceMin,
            int priceMax,
            int growthPercent,
            Random random
    ) {
        if (intervalTicks < 1) {
            throw new IllegalArgumentException("intervalTicks must be at least 1");
        }
        if (foodIntervalTicks < 1) {
            throw new IllegalArgumentException("foodIntervalTicks must be at least 1");
        }
        if (productionChance < 0 || productionChance > 1) {
            throw new IllegalArgumentException("productionChance must be between 0 and 1");
        }
        if (productionMin < 1) {
            throw new IllegalArgumentException("productionMin must be at least 1");
        }
        if (productionMax < productionMin) {
            throw new IllegalArgumentException("productionMax must be >= productionMin");
        }
        if (growthGoodsThreshold < 1) {
            throw new IllegalArgumentException("growthGoodsThreshold must be at least 1");
        }
        if (spoilageThreshold < 1) {
            throw new IllegalArgumentException("spoilageThreshold must be at least 1");
        }
        if (priceNeedThreshold < 0) {
            throw new IllegalArgumentException("priceNeedThreshold must be at least 0");
        }
        if (priceSurplusThreshold <= priceNeedThreshold) {
            throw new IllegalArgumentException("priceSurplusThreshold must be greater than priceNeedThreshold");
        }
        if (priceMin < 1) {
            throw new IllegalArgumentException("priceMin must be at least 1");
        }
        if (priceMax < priceMin) {
            throw new IllegalArgumentException("priceMax must be >= priceMin");
        }
        if (growthPercent < 1 || growthPercent > 100) {
            throw new IllegalArgumentException("growthPercent must be between 1 and 100");
        }
        this.islandStatusMongoRepository = islandStatusMongoRepository;
        this.intervalTicks = intervalTicks;
        this.foodIntervalTicks = foodIntervalTicks;
        this.productionChance = productionChance;
        this.productionMin = productionMin;
        this.productionMax = productionMax;
        this.growthGoodsThreshold = growthGoodsThreshold;
        this.spoilageThreshold = spoilageThreshold;
        this.priceNeedThreshold = priceNeedThreshold;
        this.priceSurplusThreshold = priceSurplusThreshold;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.growthPercent = growthPercent;
        this.random = random;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public boolean applyIfDue(long tick) {
        boolean economyDue = this.due(tick, this.intervalTicks);
        boolean foodDue = this.due(tick, this.foodIntervalTicks);
        if (!economyDue && !foodDue) {
            return false;
        }
        List<IslandStatusDocument> changed = new ArrayList<>();
        for (IslandStatusDocument document : this.islandStatusMongoRepository.findAll()) {
            IslandStatus current = document.toIslandStatus();
            IslandStatus next = this.applyTo(current, economyDue, foodDue);
            if (!next.equals(current)) {
                changed.add(IslandStatusDocument.from(next));
            }
        }
        if (changed.isEmpty()) {
            return false;
        }
        this.islandStatusMongoRepository.saveAll(changed);
        return true;
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private IslandStatus applyTo(IslandStatus status, boolean economyDue, boolean foodDue) {
        IslandStatus next = status;
        if (economyDue && this.productionChance > 0) {
            for (GoodType good : GoodType.tradeableGoods()) {
                double chance = good == GoodType.FOOD
                        ? Math.min(1.0, this.productionChance * FOOD_PRODUCTION_WEIGHT)
                        : this.productionChance;
                if (this.random.nextDouble() >= chance) {
                    continue;
                }
                int amount = this.productionMin + this.random.nextInt(this.productionMax - this.productionMin + 1);
                next = next.produce(good, amount);
            }
        }
        if (economyDue) {
            next = next.spoilOverstockedGoods(this.spoilageThreshold);
        }
        if (foodDue) {
            next = next.consumeFoodOrStarve();
        }
        if (economyDue && next.canFlourish(this.growthGoodsThreshold)) {
            next = next.consumeHalfGoodsAndGrow(this.growthAmount(next.population()));
        }
        if (economyDue) {
            next = next.adjustPrices(
                    this.priceNeedThreshold,
                    this.priceSurplusThreshold,
                    this.priceMin,
                    this.priceMax
            );
        }
        return next;
    }

    private long growthAmount(long population) {
        return Math.max(1L, (population * this.growthPercent + 99) / 100);
    }

    private boolean due(long tick, int interval) {
        return tick != 0 && tick % interval == 0;
    }
}
