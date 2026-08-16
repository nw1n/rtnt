package com.example.rtnt.persistence.island;

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
}
