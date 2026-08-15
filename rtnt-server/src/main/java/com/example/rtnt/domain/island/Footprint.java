package com.example.rtnt.domain.island;

public final class Footprint {
    private final int x;
    private final int y;
    private final int width;
    private final int length;

    private Footprint(int x, int y, int width, int length) {
        if (width <= 0 || length <= 0) {
            throw new IllegalArgumentException("Footprint width and length must be greater than 0");
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
        int x2Max = other.x + other.width;
        int y2Max = other.y + other.length;
        return !(x1Max < other.x || x2Max < x1Min || y1Max < other.y || y2Max < y1Min);
    }
}
