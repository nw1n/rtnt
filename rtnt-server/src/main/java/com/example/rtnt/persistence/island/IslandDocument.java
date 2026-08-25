package com.example.rtnt.persistence.island;

import com.example.rtnt.domain.island.Island;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "islands")
@NullMarked
public record IslandDocument(
        @Id String id,
        String name,
        FootprintDocument footprint
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static IslandDocument fromIsland(Island island) {
        return new IslandDocument(island.id(), island.name(), FootprintDocument.from(island.footprint()));
    }

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public Island toIsland() {
        return Island.existing(id, name, footprint.toFootprint());
    }
}
