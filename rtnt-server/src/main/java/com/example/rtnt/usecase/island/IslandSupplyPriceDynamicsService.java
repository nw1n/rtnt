package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adjusts each island's trade prices from local stock vs a target band: surplus pushes prices down,
 * shortage pulls them up. Prices stay within {@code [minPrice, maxPrice]}.
 */
@Component
public class IslandSupplyPriceDynamicsService {

    private static final Logger log = LoggerFactory.getLogger(IslandSupplyPriceDynamicsService.class);
    private static final long DEFAULT_INTERVAL_MS = 15_000L;
    private static final int DEFAULT_TARGET_STOCK_LOW = 4;
    private static final int DEFAULT_TARGET_STOCK_HIGH = 20;
    private static final int DEFAULT_ADJUSTMENT_STEP = 1;
    private static final int DEFAULT_MIN_PRICE = 1;
    private static final int DEFAULT_MAX_PRICE = 100;

    private final IslandRepository islandRepository;
    private final int targetStockLow;
    private final int targetStockHigh;
    private final int adjustmentStep;
    private final int minPrice;
    private final int maxPrice;

    public IslandSupplyPriceDynamicsService(
            IslandRepository islandRepository,
            @Value("${rtnt.island-prices.target-stock-low:" + DEFAULT_TARGET_STOCK_LOW + "}") int targetStockLow,
            @Value("${rtnt.island-prices.target-stock-high:" + DEFAULT_TARGET_STOCK_HIGH + "}") int targetStockHigh,
            @Value("${rtnt.island-prices.adjustment-step:" + DEFAULT_ADJUSTMENT_STEP + "}") int adjustmentStep,
            @Value("${rtnt.island-prices.min-price:" + DEFAULT_MIN_PRICE + "}") int minPrice,
            @Value("${rtnt.island-prices.max-price:" + DEFAULT_MAX_PRICE + "}") int maxPrice
    ) {
        if (targetStockLow > targetStockHigh) {
            throw new IllegalArgumentException("rtnt.island-prices.target-stock-low must be <= target-stock-high");
        }
        if (adjustmentStep <= 0) {
            throw new IllegalArgumentException("rtnt.island-prices.adjustment-step must be > 0");
        }
        if (minPrice <= 0 || maxPrice < minPrice) {
            throw new IllegalArgumentException("rtnt.island-prices.min-price/max-price invalid");
        }
        this.islandRepository = islandRepository;
        this.targetStockLow = targetStockLow;
        this.targetStockHigh = targetStockHigh;
        this.adjustmentStep = adjustmentStep;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    @Scheduled(
            fixedRateString = "${rtnt.island-prices.interval-ms:" + DEFAULT_INTERVAL_MS + "}",
            initialDelayString = "${rtnt.island-prices.initial-delay-ms:0}"
    )
    public void adjustPricesTick() {
        for (Island island : this.islandRepository.findAll()) {
            if (this.applySupplyPricingToIsland(island)) {
                this.islandRepository.save(island);
                log.debug("Adjusted trade prices at island {}", island.getName());
            }
        }
    }

    /**
     * @return true if any price changed
     */
    boolean applySupplyPricingToIsland(Island island) {
        boolean changed = false;
        for (GoodType good : GoodType.tradeableGoods()) {
            int stock = island.getInventory().getAmount(good);
            int current = island.getTradePrices().getPrice(good);
            int next = newPriceForSupply(
                    current,
                    stock,
                    this.targetStockLow,
                    this.targetStockHigh,
                    this.adjustmentStep,
                    this.minPrice,
                    this.maxPrice
            );
            if (next != current) {
                island.getTradePrices().setPrice(good, next);
                changed = true;
                log.trace("{} at {}: stock {} price {} -> {}", good, island.getName(), stock, current, next);
            }
        }
        return changed;
    }

    static int newPriceForSupply(
            int currentPrice,
            int stock,
            int targetStockLow,
            int targetStockHigh,
            int step,
            int minPrice,
            int maxPrice
    ) {
        if (targetStockLow > targetStockHigh) {
            throw new IllegalArgumentException("targetStockLow must be <= targetStockHigh");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("step must be > 0");
        }
        if (minPrice <= 0 || maxPrice < minPrice) {
            throw new IllegalArgumentException("invalid min/max price bounds");
        }
        if (stock > targetStockHigh) {
            return clamp(currentPrice - step, minPrice, maxPrice);
        }
        if (stock < targetStockLow) {
            return clamp(currentPrice + step, minPrice, maxPrice);
        }
        return currentPrice;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
