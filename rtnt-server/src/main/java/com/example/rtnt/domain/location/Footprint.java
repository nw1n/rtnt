package com.example.rtnt.domain.location;

import java.util.Objects;

/**
 * Value object describing an occupied rectangular area on the map.
 */
public final class Footprint {
    private final int x;
    private final int y;
    private final int width;
    private final int length;

    private Footprint(int x, int y, int width, int length) {
        if (width <= 0) {
            throw new IllegalArgumentException("Footprint width must be greater than 0");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("Footprint length must be greater than 0");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.length = length;
    }

    public static Footprint create(int x, int y, int width, int length) {
        return new Footprint(x, y, width, length);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getLength() {
        return this.length;
    }

    public boolean overlapsOrTooClose(Footprint other, int minDistance) {
        int x1Min = this.x - minDistance;
        int x1Max = this.x + this.width + minDistance;
        int y1Min = this.y - minDistance;
        int y1Max = this.y + this.length + minDistance;

        int x2Min = other.x;
        int x2Max = other.x + other.width;
        int y2Min = other.y;
        int y2Max = other.y + other.length;

        return !(x1Max < x2Min || x2Max < x1Min || y1Max < y2Min || y2Max < y1Min);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Footprint footprint)) return false;
        return this.x == footprint.x && this.y == footprint.y && this.width == footprint.width && this.length == footprint.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.width, this.length);
    }

    @Override
    public String toString() {
        return "Footprint{x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", length=" + this.length + "}";
    }
}
