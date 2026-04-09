package com.example.rtnt.adapter.out.persistence.journey;

import com.example.rtnt.domain.ship.Journey;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "journeys")
public class JourneyDocument {
    @Id
    private String id;
    private String shipId;
    private String startIslandId;
    private String targetIslandId;
    @Indexed
    private Instant departed;
    private Instant arrived;
    private Instant estimatedArrival;
    /** Null/false in legacy documents: not active until set true on departure. */
    private Boolean active;

    public JourneyDocument() {
    }

    public JourneyDocument(
            String id,
            String shipId,
            String startIslandId,
            String targetIslandId,
            Instant departed,
            Instant arrived,
            Instant estimatedArrival,
            Boolean active
    ) {
        this.id = id;
        this.shipId = shipId;
        this.startIslandId = startIslandId;
        this.targetIslandId = targetIslandId;
        this.departed = departed;
        this.arrived = arrived;
        this.estimatedArrival = estimatedArrival;
        this.active = active;
    }

    public static JourneyDocument fromDomain(Journey journey) {
        if (journey == null) {
            return null;
        }
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

    public Journey toDomain() {
        return new Journey(
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

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShipId() {
        return this.shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public String getStartIslandId() {
        return this.startIslandId;
    }

    public void setStartIslandId(String startIslandId) {
        this.startIslandId = startIslandId;
    }

    public String getTargetIslandId() {
        return this.targetIslandId;
    }

    public void setTargetIslandId(String targetIslandId) {
        this.targetIslandId = targetIslandId;
    }

    public Instant getDeparted() {
        return this.departed;
    }

    public void setDeparted(Instant departed) {
        this.departed = departed;
    }

    public Instant getArrived() {
        return this.arrived;
    }

    public void setArrived(Instant arrived) {
        this.arrived = arrived;
    }

    public Instant getEstimatedArrival() {
        return this.estimatedArrival;
    }

    public void setEstimatedArrival(Instant estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public Boolean getActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
