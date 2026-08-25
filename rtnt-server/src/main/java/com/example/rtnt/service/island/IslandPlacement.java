package com.example.rtnt.service.island;

import com.example.rtnt.domain.island.Footprint;
import com.example.rtnt.domain.island.Island;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public final class IslandPlacement {
    private static final int MIN_WIDTH = 20;
    private static final int MAX_WIDTH = 100;
    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 100;
    private static final int MAX_ATTEMPTS = 10_000;
    private static final int MIN_DISTANCE = 10;

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final int mapWidth;
    private final int mapHeight;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public IslandPlacement(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Island placeIsland(String name, List<Island> existing) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Island island = tryCreateValidIsland(name, existing);
            if (island != null) {
                return island;
            }
        }
        throw new IllegalStateException("Could not place island after " + MAX_ATTEMPTS + " attempts");
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private @Nullable Island tryCreateValidIsland(String name, List<Island> existing) {
        Footprint candidateFootprint = createRandomFootprint();
        boolean isBlocked = existing.stream()
                .anyMatch(island -> isDistanceTooClose(candidateFootprint, island.footprint(), MIN_DISTANCE));
        if (isBlocked) {
            return null;
        }
        return Island.create(name, candidateFootprint);
    }

    private Footprint createRandomFootprint() {
        int width = randomWidth();
        int length = randomLength();
        int x = randomX(width);
        int y = randomY(length);
        return new Footprint(x, y, width, length);
    }

    /***************************************************************************
     *                                                                         *
     * Static Utilities                                                        *
     *                                                                         *
     **************************************************************************/

    private static int randomWidth() {
        return randomInRange(MIN_WIDTH, MAX_WIDTH);
    }

    private static int randomLength() {
        return randomInRange(MIN_LENGTH, MAX_LENGTH);
    }

    private int randomX(int width) {
        return randomInRange(0, this.mapWidth - width);
    }

    private int randomY(int length) {
        return randomInRange(0, this.mapHeight - length);
    }

    private static int randomInRange(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static boolean isDistanceTooClose(Footprint footprint, Footprint other, int minDistance) {
        int x1Min = footprint.x() - minDistance;
        int x1Max = footprint.x() + footprint.width() + minDistance;
        int y1Min = footprint.y() - minDistance;
        int y1Max = footprint.y() + footprint.length() + minDistance;
        int x2Max = other.x() + other.width();
        int y2Max = other.y() + other.length();
        var isDistanceSufficient = x1Max < other.x() || x2Max < x1Min || y1Max < other.y() || y2Max < y1Min;
        return !isDistanceSufficient;
    }
}
