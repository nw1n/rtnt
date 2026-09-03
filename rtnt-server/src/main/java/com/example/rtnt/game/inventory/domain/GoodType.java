package com.example.rtnt.game.inventory.domain;

import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@NullMarked
public enum GoodType {
    GOLD(false),
    RUM(true),
    SUGAR(true),
    SPICES(true),
    TOBACCO(true);

    private final boolean tradeable;

    GoodType(boolean tradeable) {
        this.tradeable = tradeable;
    }

    public boolean isTradeable() {
        return this.tradeable;
    }

    public static Set<GoodType> tradeableGoods() {
        EnumSet<GoodType> result = EnumSet.noneOf(GoodType.class);
        for (GoodType goodType : GoodType.values()) {
            if (goodType.isTradeable()) {
                result.add(goodType);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
