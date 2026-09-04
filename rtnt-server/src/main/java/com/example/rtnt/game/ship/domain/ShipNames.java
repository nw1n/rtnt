package com.example.rtnt.game.ship.domain;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@NullMarked
public class ShipNames {

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<String> names;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public ShipNames(List<String> names) {
        this.names = List.copyOf(names);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public String next() {
        int i = this.currentIndex.getAndIncrement();
        if (i < this.names.size()) {
            return this.names.get(i);
        }
        return "Ship " + (i + 1);
    }
}
