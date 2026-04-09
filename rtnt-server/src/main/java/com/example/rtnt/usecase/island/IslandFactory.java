package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Use-case service for creating islands with configured naming strategies.
 */
@Component
public class IslandFactory {
    private static final int MIN_WIDTH = 20;
    private static final int MAX_WIDTH = 100;
    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 100;
    private static final int MAX_ATTEMPTS = 1_000;
    private static final int MIN_DISTANCE = 10;
    private static final int MAX_X_COORDINATE = 2_000;
    private static final int MAX_Y_COORDINATE = 1_000;

    private final IslandNameProvider islandNameProvider;

    public IslandFactory(IslandNameProvider islandNameProvider) {
        this.islandNameProvider = islandNameProvider;
    }

    /**
     * Create a new island with a name selected from the Caribbean island names.
     * Uses the next name in sequence.
     *
     * @param footprint the footprint of the island
     * @return a new Island with a Caribbean name
     */
    public Island createWithCaribbeanName(Footprint footprint) {
        String name = this.islandNameProvider.getNextName();
        return StandardIsland.create(name, footprint, this.defaultIslandInventory(), this.defaultIslandTradePrices());
    }

    /**
     * Create a new island with a random name from the Caribbean island names.
     *
     * @param footprint the footprint of the island
     * @return a new Island with a random Caribbean name
     */
    public Island createWithRandomCaribbeanName(Footprint footprint) {
        String name = this.islandNameProvider.getRandomName();
        return StandardIsland.create(name, footprint, this.defaultIslandInventory(), this.defaultIslandTradePrices());
    }

    /**
     * Create a new island with a specific name.
     * This method allows you to bypass the name selection logic if needed.
     *
     * @param name the name for the island
     * @param footprint the footprint of the island
     * @return a new Island with the specified name
     */
    public Island createWithName(String name, Footprint footprint) {
        return StandardIsland.create(name, footprint, this.defaultIslandInventory(), this.defaultIslandTradePrices());
    }

    public Island createRandomizedWithName(String name, List<Island> existingIslands) {
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            int width = this.randomInRange(MIN_WIDTH, MAX_WIDTH);
            int length = this.randomInRange(MIN_LENGTH, MAX_LENGTH);
            int x = ThreadLocalRandom.current().nextInt(MAX_X_COORDINATE - width + 1);
            int y = ThreadLocalRandom.current().nextInt(MAX_Y_COORDINATE - length + 1);

            Island candidate = StandardIsland.create(
                    name,
                    Footprint.create(x, y, width, length),
                    this.defaultIslandInventory(),
                    this.defaultIslandTradePrices()
            );

            if (!this.overlapsOrTooClose(candidate, existingIslands)) {
                return candidate;
            }

            attempts++;
        }

        throw new IllegalStateException("Could not find a non-overlapping position after " + MAX_ATTEMPTS + " attempts");
    }

    public Island createRandomizedWithCaribbeanName(List<Island> existingIslands) {
        return this.createRandomizedWithName(this.islandNameProvider.getNextName(), existingIslands);
    }

    private Inventory defaultIslandInventory() {
        return Inventory.of(Map.of(
                GoodType.GOLD, 100,
                GoodType.RUM, 10,
                GoodType.SUGAR, 10,
                GoodType.SPICES, 10,
                GoodType.TOBACCO, 10
        ));
    }

    private TradePriceList defaultIslandTradePrices() {
        return TradePriceList.of(Map.of(
                GoodType.RUM, 3,
                GoodType.SUGAR, 2,
                GoodType.SPICES, 4,
                GoodType.TOBACCO, 5
        ));
    }

    private int randomInRange(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private boolean overlapsOrTooClose(Island candidate, List<Island> existingIslands) {
        for (Island existing : existingIslands) {
            if (this.overlapsOrTooClose(candidate, existing)) {
                return true;
            }
        }
        return false;
    }

    private boolean overlapsOrTooClose(Island island1, Island island2) {
        return island1.getFootprint().overlapsOrTooClose(island2.getFootprint(), MIN_DISTANCE);
    }
}
