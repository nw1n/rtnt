package com.example.rtnt.game.ship.persistence;

import com.example.rtnt.game.ship.domain.Ship;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ships")
@NullMarked
public record ShipDocument(
        @Id String id,
        String name,
        @Nullable String islandId,
        @Nullable String playerId
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static ShipDocument from(Ship ship) {
        return new ShipDocument(ship.getId(), ship.getName(), ship.getIslandId(), ship.getPlayerId());
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Ship toShip() {
        return Ship.existing(this.id, this.name, this.islandId, this.playerId);
    }
}
