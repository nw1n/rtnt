package com.example.rtnt.game.island.persistence;

import com.example.rtnt.game.island.domain.Island;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "islands")
@NullMarked
public record IslandDocument(
        @Id String id,
        String name,
        FootprintDocument footprint,
        @Nullable TradePriceListDocument tradePrices
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static IslandDocument fromIsland(Island island) {
        return new IslandDocument(
                island.id(),
                island.name(),
                FootprintDocument.from(island.footprint()),
                TradePriceListDocument.from(island.tradePrices())
        );
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Island toIsland() {
        return Island.existing(
                this.id,
                this.name,
                this.footprint.toFootprint(),
                TradePriceListDocument.toTradePriceList(this.tradePrices)
        );
    }
}
