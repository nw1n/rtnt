package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * NPC ships trade at islands: sell once after arriving, buy once before leaving.
 * Invoked from {@link ShipDepartureService} (not on a timer).
 */
@Component
public class NpcShipTradingService {

    private static final Logger log = LoggerFactory.getLogger(NpcShipTradingService.class);

    private static final GoodType[] TRADEABLE_GOODS =
            GoodType.tradeableGoods().toArray(new GoodType[0]);

    private final ShipRepository shipRepository;
    private final IslandRepository islandRepository;
    private final TradeWithIslandUseCase tradeWithIslandUseCase;

    public NpcShipTradingService(
            ShipRepository shipRepository,
            IslandRepository islandRepository,
            TradeWithIslandUseCase tradeWithIslandUseCase
    ) {
        this.shipRepository = shipRepository;
        this.islandRepository = islandRepository;
        this.tradeWithIslandUseCase = tradeWithIslandUseCase;
    }

    /**
     * Before departure: NPC buys one random affordable good at the current island (if any).
     */
    public void tryNpcBuyOnce(String shipId) {
        Ship ship = this.shipRepository.findById(shipId).orElse(null);
        if (ship == null || isPlayerControlled(ship)) {
            return;
        }
        Island island = this.resolveAnchoredIsland(ship);
        if (island == null) {
            return;
        }
        this.tryOneBuy(ship, island);
    }

    /**
     * After arrival: NPC sells one random good the island can afford (if any).
     */
    public void tryNpcSellOnce(String shipId) {
        Ship ship = this.shipRepository.findById(shipId).orElse(null);
        if (ship == null || isPlayerControlled(ship)) {
            return;
        }
        Island island = this.resolveAnchoredIsland(ship);
        if (island == null) {
            return;
        }
        this.tryOneSell(ship, island);
    }

    private Island resolveAnchoredIsland(Ship ship) {
        String islandId = ship.getIslandId();
        if (islandId == null || islandId.isBlank()) {
            return null;
        }
        return this.islandRepository.findById(islandId).orElse(null);
    }

    private void tryOneBuy(Ship ship, Island island) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (GoodType good : shuffledTradeableGoods(rnd)) {
            int max = maxAffordableBuyAmount(ship, island, good);
            if (max < 1) {
                continue;
            }
            int amount = 1 + rnd.nextInt(max);
            try {
                this.tradeWithIslandUseCase.buyFromIsland(ship.getId(), good, amount);
                log.debug("NPC ship {} bought {} {} at island {}", ship.getName(), amount, good, island.getName());
            } catch (RuntimeException ex) {
                log.debug("NPC buy skipped for ship {}: {}", ship.getId(), ex.getMessage());
            }
            return;
        }
    }

    private void tryOneSell(Ship ship, Island island) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (GoodType good : shuffledTradeableGoods(rnd)) {
            int max = maxAffordableSellAmount(ship, island, good);
            if (max < 1) {
                continue;
            }
            int amount = 1 + rnd.nextInt(max);
            try {
                this.tradeWithIslandUseCase.sellToIsland(ship.getId(), good, amount);
                log.debug("NPC ship {} sold {} {} at island {}", ship.getName(), amount, good, island.getName());
            } catch (RuntimeException ex) {
                log.debug("NPC sell skipped for ship {}: {}", ship.getId(), ex.getMessage());
            }
            return;
        }
    }

    static int maxAffordableBuyAmount(Ship ship, Island island, GoodType good) {
        int unitPrice = island.getTradePrices().getPrice(good);
        if (unitPrice <= 0) {
            return 0;
        }
        int holdRoom = ship.getCargoCapacity() - ship.getInventory().sumTradeableGoods();
        int islandStock = island.getInventory().getAmount(good);
        int shipGold = ship.getInventory().getAmount(GoodType.GOLD);
        int maxByGold = shipGold / unitPrice;
        return Math.min(holdRoom, Math.min(islandStock, maxByGold));
    }

    static int maxAffordableSellAmount(Ship ship, Island island, GoodType good) {
        int unitPrice = island.getTradePrices().getPrice(good);
        if (unitPrice <= 0) {
            return 0;
        }
        int shipStock = ship.getInventory().getAmount(good);
        int islandGold = island.getInventory().getAmount(GoodType.GOLD);
        int maxByIslandGold = islandGold / unitPrice;
        return Math.min(shipStock, maxByIslandGold);
    }

    private static List<GoodType> shuffledTradeableGoods(ThreadLocalRandom rnd) {
        List<GoodType> list = new ArrayList<>(Arrays.asList(TRADEABLE_GOODS));
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            GoodType tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        return list;
    }

    private static boolean isPlayerControlled(Ship ship) {
        return ship.getPlayerId() != null && !ship.getPlayerId().isBlank();
    }
}
