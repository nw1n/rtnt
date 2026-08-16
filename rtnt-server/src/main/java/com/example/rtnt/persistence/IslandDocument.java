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
    private FootprintDocument footprint;

    public IslandDocument() {
    }

    public IslandDocument(String id, String name, FootprintDocument footprint) {
        this.id = id;
        this.name = name;
        this.footprint = footprint;
    }

    public static IslandDocument fromIsland(Island island) {
        return new IslandDocument(island.id(), island.name(), FootprintDocument.from(island.footprint()));
    }

    public Island toIsland() {
        return Island.existing(this.id, this.name, this.footprint.toFootprint());
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

    public FootprintDocument getFootprint() {
        return this.footprint;
    }

    public void setFootprint(FootprintDocument footprint) {
        this.footprint = footprint;
    }

    public static class FootprintDocument {
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

        static FootprintDocument from(Footprint footprint) {
            return new FootprintDocument(footprint.x(), footprint.y(), footprint.width(), footprint.length());
        }

        Footprint toFootprint() {
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
}
