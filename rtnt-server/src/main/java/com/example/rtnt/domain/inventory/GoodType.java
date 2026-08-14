package com.example.rtnt.domain.inventory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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
