package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.usecase.ship.ListJourneysUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
public class JourneyController {

    private final ListJourneysUseCase listJourneysUseCase;

    public JourneyController(ListJourneysUseCase listJourneysUseCase) {
        this.listJourneysUseCase = listJourneysUseCase;
    }

    @GetMapping
    public List<JourneyResponse> getAll(@RequestParam(required = false) String shipId) {
        List<Journey> journeys = (shipId != null && !shipId.isBlank())
                ? this.listJourneysUseCase.getJourneysForShip(shipId)
                : this.listJourneysUseCase.getLatestJourneys();
        return journeys.stream().map(JourneyResponse::fromDomain).toList();
    }

    public record JourneyResponse(
            String id,
            String shipId,
            String startIslandId,
            String targetIslandId,
            String departed,
            String arrived,
            String estimatedArrival,
            boolean active
    ) {
        static JourneyResponse fromDomain(Journey journey) {
            return new JourneyResponse(
                    journey.id(),
                    journey.shipId(),
                    journey.startIslandId(),
                    journey.targetIslandId(),
                    asIsoString(journey.departed()),
                    asIsoString(journey.arrived()),
                    asIsoString(journey.estimatedArrival()),
                    journey.active()
            );
        }

        private static String asIsoString(java.time.Instant value) {
            return value == null ? null : value.toString();
        }
    }
}
