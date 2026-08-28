package com.example.rtnt.game.island.domain;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@NullMarked
public class IslandNames {

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

    public IslandNames(List<String> names) {
        this.names = List.copyOf(names);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public String next() {
        int i = this.currentIndex.getAndIncrement();
        var isFreeNameLeft = i < this.names.size();

        if (isFreeNameLeft) {
            return this.names.get(i);
        } else {
            return "Island " + (i + 1);
        }
    }
}
