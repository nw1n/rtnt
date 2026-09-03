package com.example.rtnt.game.ship.persistence;

import com.example.rtnt.game.ship.domain.Journey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "journeys")
@NullMarked
public record JourneyDocument(
        @Id String id,
        String shipId,
        String startIslandId,
        String targetIslandId,
        Instant departed,
        @Nullable Instant arrived,
        Instant estimatedArrival,
        @Nullable Boolean active
) {
    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static JourneyDocument from(Journey journey) {
        return new JourneyDocument(
                journey.id(),
                journey.shipId(),
                journey.startIslandId(),
                journey.targetIslandId(),
                journey.departed(),
                journey.arrived(),
                journey.estimatedArrival(),
                journey.active()
        );
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public Journey toJourney() {
        return Journey.existing(
                this.id,
                this.shipId,
                this.startIslandId,
                this.targetIslandId,
                this.departed,
                this.arrived,
                this.estimatedArrival,
                Journey.resolveActiveFromStorage(this.active)
        );
    }
}
