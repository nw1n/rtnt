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
    private static final List<GoodType> TRADEABLE_GOODS = List.copyOf(GoodType.tradeableGoods());

    private final IslandStatusMongoRepository islandStatusMongoRepository;
    private final int intervalTicks;
    private final double productionChance;
    private final int productionMin;
    private final int productionMax;
    private final int growthGoodsThreshold;
    private final int growthMin;
    private final int growthMax;
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
            @Value("${rtnt.island.production-chance:0.25}") double productionChance,
            @Value("${rtnt.island.production-min:1}") int productionMin,
            @Value("${rtnt.island.production-max:5}") int productionMax,
            @Value("${rtnt.island.growth-goods-threshold:20}") int growthGoodsThreshold,
            @Value("${rtnt.island.population-growth-min:1}") int growthMin,
            @Value("${rtnt.island.population-growth-max:3}") int growthMax
    ) {
        this(
                islandStatusMongoRepository,
                intervalTicks,
                productionChance,
                productionMin,
                productionMax,
                growthGoodsThreshold,
                growthMin,
                growthMax,
                new Random()
        );
    }

    IslandEconomy(
            IslandStatusMongoRepository islandStatusMongoRepository,
            int intervalTicks,
            double productionChance,
            int productionMin,
            int productionMax,
            int growthGoodsThreshold,
            int growthMin,
            int growthMax,
            Random random
    ) {
        if (intervalTicks < 1) {
            throw new IllegalArgumentException("intervalTicks must be at least 1");
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
        if (growthMin < 1) {
            throw new IllegalArgumentException("growthMin must be at least 1");
        }
        if (growthMax < growthMin) {
            throw new IllegalArgumentException("growthMax must be >= growthMin");
        }
        this.islandStatusMongoRepository = islandStatusMongoRepository;
        this.intervalTicks = intervalTicks;
        this.productionChance = productionChance;
        this.productionMin = productionMin;
        this.productionMax = productionMax;
        this.growthGoodsThreshold = growthGoodsThreshold;
        this.growthMin = growthMin;
        this.growthMax = growthMax;
        this.random = random;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public boolean applyIfDue(long tick) {
        if (tick == 0 || tick % this.intervalTicks != 0) {
            return false;
        }
        List<IslandStatusDocument> changed = new ArrayList<>();
        for (IslandStatusDocument document : this.islandStatusMongoRepository.findAll()) {
            IslandStatus current = document.toIslandStatus();
            IslandStatus next = this.applyTo(current);
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

    private IslandStatus applyTo(IslandStatus status) {
        IslandStatus next = status;
        if (this.productionChance > 0 && this.random.nextDouble() < this.productionChance) {
            GoodType good = TRADEABLE_GOODS.get(this.random.nextInt(TRADEABLE_GOODS.size()));
            int amount = this.productionMin + this.random.nextInt(this.productionMax - this.productionMin + 1);
            next = next.produce(good, amount);
        }
        if (next.canFlourish(this.growthGoodsThreshold)) {
            int growth = this.growthMin + this.random.nextInt(this.growthMax - this.growthMin + 1);
            next = next.consumeHalfGoodsAndGrow(growth);
        }
        return next;
    }
}
