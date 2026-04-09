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
 * When an island holds more than a threshold of a tradeable good, surplus is "consumed": stock is halved
 * (rounded down). Gold is never affected.
 */
@Component
public class IslandOverstockConsumeService {

    private static final Logger log = LoggerFactory.getLogger(IslandOverstockConsumeService.class);
    private static final long DEFAULT_INTERVAL_MS = 10_000L;
    private static final int DEFAULT_THRESHOLD = 100;

    private final IslandRepository islandRepository;
    private final int overstockThreshold;

    public IslandOverstockConsumeService(
            IslandRepository islandRepository,
            @Value("${rtnt.island-consume.threshold:" + DEFAULT_THRESHOLD + "}") int overstockThreshold
    ) {
        this.islandRepository = islandRepository;
        this.overstockThreshold = overstockThreshold;
    }

    @Scheduled(
            fixedRateString = "${rtnt.island-consume.interval-ms:" + DEFAULT_INTERVAL_MS + "}",
            initialDelayString = "${rtnt.island-consume.initial-delay-ms:0}"
    )
    public void consumeSurplusTick() {
        for (Island island : this.islandRepository.findAll()) {
            if (this.applyConsumeToIsland(island)) {
                this.islandRepository.save(island);
                log.info("Surplus consumed at island {}", island.getName());
            }
        }
    }

    /**
     * Halves amounts strictly greater than {@code threshold} (integer half, rounded down).
     *
     * @return true if any good was changed
     */
    boolean applyConsumeToIsland(Island island) {
        boolean changed = false;
        for (GoodType good : GoodType.tradeableGoods()) {
            int amount = island.getInventory().getAmount(good);
            int after = amountAfterConsume(amount, this.overstockThreshold);
            if (after != amount) {
                island.getInventory().setAmount(good, after);
                changed = true;
                log.debug("{} at {}: {} -> {}", good, island.getName(), amount, after);
            }
        }
        return changed;
    }

    static int amountAfterConsume(int current, int threshold) {
        if (current <= threshold) {
            return current;
        }
        return current / 2;
    }
}
