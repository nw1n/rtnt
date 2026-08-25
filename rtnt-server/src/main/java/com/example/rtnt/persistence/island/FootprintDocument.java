package com.example.rtnt.persistence.island;

import com.example.rtnt.domain.island.Footprint;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record FootprintDocument(int x, int y, int width, int length) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static FootprintDocument from(Footprint footprint) {
        return new FootprintDocument(footprint.x(), footprint.y(), footprint.width(), footprint.length());
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Footprint toFootprint() {
        return new Footprint(x, y, width, length);
    }
}
