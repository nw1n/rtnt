package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class IslandProductionService {

    private static final Logger log = LoggerFactory.getLogger(IslandProductionService.class);
    private static final long DEFAULT_PRODUCTION_INTERVAL_MS = 10_000L;
    private static final double DEFAULT_BOOM_PROBABILITY = 0.02D;
    private static final int DEFAULT_BOOM_AMOUNT = 30;
    private static final GoodType[] TRADEABLE_GOODS = GoodType.tradeableGoods().toArray(new GoodType[0]);

    private final IslandRepository islandRepository;
    private final double boomProbability;
    private final int boomAmount;

    public IslandProductionService(
            IslandRepository islandRepository,
            @Value("${rtnt.island-production.boom-probability:" + DEFAULT_BOOM_PROBABILITY + "}") double boomProbability,
            @Value("${rtnt.island-production.boom-amount:" + DEFAULT_BOOM_AMOUNT + "}") int boomAmount
    ) {
        this.islandRepository = islandRepository;
        this.boomProbability = boomProbability;
        this.boomAmount = boomAmount;
    }

    @Scheduled(fixedRateString = "${rtnt.island-production.interval-ms:" + DEFAULT_PRODUCTION_INTERVAL_MS + "}")
    public void produceGoodsTick() {
        List<Island> islands = this.islandRepository.findAll();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (Island island : islands) {
            if (!this.rollBoom(rnd)) {
                continue;
            }
            GoodType good = pickRandomTradeableGood(rnd);
            island.getInventory().addAmount(good, this.boomAmount);
            this.islandRepository.save(island);
            log.info("Boom at {}: +{} {}", island.getName(), this.boomAmount, good);
        }
    }

    static boolean rollBoom(ThreadLocalRandom rnd, double boomProbability) {
        if (boomProbability <= 0) {
            return false;
        }
        if (boomProbability >= 1) {
            return true;
        }
        return rnd.nextDouble() < boomProbability;
    }

    private boolean rollBoom(ThreadLocalRandom rnd) {
        return rollBoom(rnd, this.boomProbability);
    }

    private static GoodType pickRandomTradeableGood(ThreadLocalRandom rnd) {
        int index = rnd.nextInt(TRADEABLE_GOODS.length);
        return TRADEABLE_GOODS[index];
    }
}
