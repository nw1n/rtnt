package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.JourneyRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListJourneysUseCase {
    private final JourneyRepository journeyRepository;

    public ListJourneysUseCase(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    public List<Journey> getLatestJourneys() {
        return this.journeyRepository.findLatest100();
    }

    public List<Journey> getJourneysForShip(String shipId) {
        return this.journeyRepository.findByShipId(shipId);
    }
}
