package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Journey;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.domain.ShipTravel;
import com.example.rtnt.game.ship.persistence.ShipDocument;
import com.example.rtnt.game.ship.persistence.ShipMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipJourneyCheckTest {

    @Mock
    private ShipMongoRepository shipMongoRepository;

    @Mock
    private IslandService islandService;

    @Test
    void skipsTicksOffInterval() {
        ShipJourneyCheck check = this.check();

        assertFalse(check.applyIfDue(0));
        assertFalse(check.applyIfDue(9));
        verify(this.islandService, never()).list();
        verify(this.shipMongoRepository, never()).findAll();
    }

    @Test
    void sendsIdleShipToOtherIslandWithDistanceBasedEta() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Ship ship = Ship.create("Black Pearl", start.id(), null);
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertTrue(this.check().applyIfDue(10));

        Ship saved = this.capturedSavedShip();
        assertNull(saved.getIslandId());
        Journey journey = saved.getJourney();
        assertNotNull(journey);
        assertTrue(journey.active());
        assertEquals(start.id(), journey.startIslandId());
        assertEquals(target.id(), journey.targetIslandId());
        assertEquals(10, journey.departedTick());
        assertEquals(10 + ShipTravel.ticks(start.footprint(), target.footprint(), ship.getSpeed()), journey.estimatedArrivalTick());
    }

    @Test
    void doesNotDepartPlayerShips() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Ship ship = Ship.create("Player Ship", start.id(), "player-1");
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertFalse(this.check().applyIfDue(10));
        verify(this.shipMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void leavesActiveJourneyUntouchedBeforeEta() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Journey journey = Journey.create(start.id(), target.id(), 1, 20);
        Ship ship = Ship.create("Black Pearl", null, null, journey);
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertFalse(this.check().applyIfDue(10));
        verify(this.shipMongoRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void completesJourneyWhenEtaReachedAndSetsIsland() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Journey journey = Journey.create(start.id(), target.id(), 1, 10);
        Ship ship = Ship.create("Black Pearl", null, null, journey);
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertTrue(this.check().applyIfDue(10));

        Ship saved = this.capturedSavedShip();
        assertEquals(target.id(), saved.getIslandId());
        Journey completed = saved.getJourney();
        assertNotNull(completed);
        assertFalse(completed.active());
        assertEquals(10, completed.arrivedTick());
    }

    @Test
    void completesJourneyWhenEtaSurpassed() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Journey journey = Journey.create(start.id(), target.id(), 1, 8);
        Ship ship = Ship.create("Black Pearl", null, null, journey);
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertTrue(this.check().applyIfDue(10));

        Ship saved = this.capturedSavedShip();
        assertEquals(target.id(), saved.getIslandId());
        assertNotNull(saved.getJourney());
        assertFalse(saved.getJourney().active());
        assertEquals(10, saved.getJourney().arrivedTick());
    }

    private ShipJourneyCheck check() {
        return new ShipJourneyCheck(this.shipMongoRepository, this.islandService, 10, new Random(1));
    }

    @SuppressWarnings("unchecked")
    private Ship capturedSavedShip() {
        ArgumentCaptor<List<ShipDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.shipMongoRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        return captor.getValue().getFirst().toShip();
    }
}
