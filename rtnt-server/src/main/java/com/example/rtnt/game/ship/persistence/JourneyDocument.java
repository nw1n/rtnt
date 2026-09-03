package com.example.rtnt.game.ship.persistence;

import com.example.rtnt.game.ship.domain.Journey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "journeys")
@NullMarked
public record JourneyDocument(
        @Id String id,
        String shipId,
        String startIslandId,
        String targetIslandId,
        long departedTick,
        @Nullable Long arrivedTick,
        long estimatedArrivalTick,
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
                journey.departedTick(),
                journey.arrivedTick(),
                journey.estimatedArrivalTick(),
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
                this.departedTick,
                this.arrivedTick,
                this.estimatedArrivalTick,
                Journey.resolveActiveFromStorage(this.active)
        );
    }
}
