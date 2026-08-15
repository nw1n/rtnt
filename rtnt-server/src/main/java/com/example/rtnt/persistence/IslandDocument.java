package com.example.rtnt.persistence;

import com.example.rtnt.domain.island.Footprint;
import com.example.rtnt.domain.island.Island;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "islands")
public class IslandDocument {
    @Id
    private String id;
    private String name;
    private int x;
    private int y;
    private int width;
    private int length;

    public IslandDocument() {
    }

    public IslandDocument(String id, String name, int x, int y, int width, int length) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.length = length;
    }

    public static IslandDocument fromIsland(Island island) {
        Footprint footprint = island.getFootprint();
        return new IslandDocument(
                island.getId(),
                island.getName(),
                footprint.getX(),
                footprint.getY(),
                footprint.getWidth(),
                footprint.getLength()
        );
    }

    public Island toIsland() {
        return Island.existing(this.id, this.name, Footprint.create(this.x, this.y, this.width, this.length));
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
