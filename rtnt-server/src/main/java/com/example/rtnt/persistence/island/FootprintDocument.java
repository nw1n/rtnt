package com.example.rtnt.persistence.island;

import com.example.rtnt.domain.island.Footprint;

public class FootprintDocument {
    private int x;
    private int y;
    private int width;
    private int length;

    public FootprintDocument() {
    }

    public FootprintDocument(int x, int y, int width, int length) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.length = length;
    }

    public static FootprintDocument from(Footprint footprint) {
        return new FootprintDocument(footprint.x(), footprint.y(), footprint.width(), footprint.length());
    }

    public Footprint toFootprint() {
        return Footprint.create(this.x, this.y, this.width, this.length);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
