package com.example.rtnt.game.ship.persistence;

import com.example.rtnt.game.ship.domain.Journey;
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
        @Nullable String playerId,
        @Nullable JourneyDocument journey
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static ShipDocument from(Ship ship) {
        Journey journey = ship.getJourney();
        return new ShipDocument(
                ship.getId(),
                ship.getName(),
                ship.getIslandId(),
                ship.getPlayerId(),
                journey == null ? null : JourneyDocument.from(journey)
        );
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Ship toShip() {
        JourneyDocument journey = this.journey;
        return Ship.existing(
                this.id,
                this.name,
                this.islandId,
                this.playerId,
                journey == null ? null : journey.toJourney()
        );
    }
}
