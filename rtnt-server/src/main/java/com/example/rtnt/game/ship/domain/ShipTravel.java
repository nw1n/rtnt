package com.example.rtnt.game.ship.domain;

import com.example.rtnt.game.island.domain.Footprint;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ShipTravel {
    private ShipTravel() {
    }

    public static long ticks(Footprint from, Footprint to, int speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be greater than 0");
        }
        return Math.max(1L, (long) Math.ceil(distance(from, to) / speed));
    }

    public static double distance(Footprint a, Footprint b) {
        double ax = a.x() + (a.width() / 2.0);
        double ay = a.y() + (a.length() / 2.0);
        double bx = b.x() + (b.width() / 2.0);
        double by = b.y() + (b.length() / 2.0);
        return Math.hypot(ax - bx, ay - by);
    }
}
