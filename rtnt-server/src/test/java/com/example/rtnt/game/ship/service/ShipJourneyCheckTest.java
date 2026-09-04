package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Journey;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.domain.ShipTravel;
import com.example.rtnt.game.ship.persistence.ShipDocument;
import com.example.rtnt.game.ship.persistence.ShipMongoRepository;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeResult;
import com.example.rtnt.game.trade.domain.TradeType;
import com.example.rtnt.game.trade.service.ArrivalTrade;
import com.example.rtnt.game.trade.service.TradeEventStore;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipJourneyCheckTest {

    @Mock
    private ShipMongoRepository shipMongoRepository;

    @Mock
    private IslandService islandService;

    @Mock
    private ArrivalTrade arrivalTrade;

    @Mock
    private TradeEventStore tradeEventStore;

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
        when(this.islandService.listStatuses()).thenReturn(List.of());
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
        verify(this.arrivalTrade, never()).execute(any(), any(), any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void doesNotDepartPlayerShips() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Ship ship = Ship.create("Player Ship", start.id(), "player-1");
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.islandService.listStatuses()).thenReturn(List.of());
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
        when(this.islandService.listStatuses()).thenReturn(List.of());
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
        when(this.islandService.listStatuses()).thenReturn(List.of());
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertTrue(this.check().applyIfDue(10));

        Ship saved = this.capturedSavedShip();
        assertEquals(target.id(), saved.getIslandId());
        Journey completed = saved.getJourney();
        assertNotNull(completed);
        assertFalse(completed.active());
        assertEquals(10, completed.arrivedTick());
        verify(this.arrivalTrade, never()).execute(any(), any(), any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void completesJourneyWhenEtaSurpassed() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Journey journey = Journey.create(start.id(), target.id(), 1, 8);
        Ship ship = Ship.create("Black Pearl", null, null, journey);
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.islandService.listStatuses()).thenReturn(List.of());
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));

        assertTrue(this.check().applyIfDue(10));

        Ship saved = this.capturedSavedShip();
        assertEquals(target.id(), saved.getIslandId());
        assertNotNull(saved.getJourney());
        assertFalse(saved.getJourney().active());
        assertEquals(10, saved.getJourney().arrivedTick());
    }

    @Test
    void tradesWhenNpcShipArrivesWithIslandStatus() {
        Island start = Island.existing("start", "Start", new Footprint(0, 0, 10, 10));
        Island target = Island.existing("target", "Target", new Footprint(100, 0, 10, 10));
        Journey journey = Journey.create(start.id(), target.id(), 1, 10);
        Ship ship = Ship.create("Black Pearl", null, null, journey);
        IslandStatus status = IslandStatus.initial(target.id());
        when(this.islandService.list()).thenReturn(List.of(start, target));
        when(this.islandService.listStatuses()).thenReturn(List.of(status));
        when(this.shipMongoRepository.findAll()).thenReturn(List.of(ShipDocument.from(ship)));
        when(this.arrivalTrade.execute(any(Ship.class), eq(target), eq(status), eq(10L)))
                .thenAnswer(invocation -> {
                    Ship docked = invocation.getArgument(0);
                    TradeEvent event = TradeEvent.create(
                            10,
                            TradeType.SELL_TO_ISLAND,
                            docked.getId(),
                            docked.getName(),
                            target.id(),
                            target.name(),
                            GoodType.RUM,
                            2,
                            3,
                            6
                    );
                    IslandStatus after = status.withInventory(Inventory.of(java.util.Map.of(
                            GoodType.GOLD, 94,
                            GoodType.RUM, 12
                    )));
                    return new TradeResult(docked.withInventory(Inventory.of(java.util.Map.of(
                            GoodType.GOLD, 1006,
                            GoodType.RUM, 3
                    ))), after, List.of(event));
                });

        assertTrue(this.check().applyIfDue(10));

        Ship saved = this.capturedSavedShip();
        assertEquals(target.id(), saved.getIslandId());
        assertEquals(1006, saved.getInventory().getAmount(GoodType.GOLD));
        assertEquals(3, saved.getInventory().getAmount(GoodType.RUM));
        ArgumentCaptor<List<IslandStatus>> statusCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.islandService).saveStatuses(statusCaptor.capture());
        assertEquals(1, statusCaptor.getValue().size());
        assertEquals(94, statusCaptor.getValue().getFirst().inventory().getAmount(GoodType.GOLD));
        ArgumentCaptor<List<TradeEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.tradeEventStore).record(eventCaptor.capture());
        assertEquals(1, eventCaptor.getValue().size());
        assertEquals(TradeType.SELL_TO_ISLAND, eventCaptor.getValue().getFirst().tradeType());
    }

    private ShipJourneyCheck check() {
        return new ShipJourneyCheck(
                this.shipMongoRepository,
                this.islandService,
                this.arrivalTrade,
                this.tradeEventStore,
                10,
                new Random(1)
        );
    }

    @SuppressWarnings("unchecked")
    private Ship capturedSavedShip() {
        ArgumentCaptor<List<ShipDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.shipMongoRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        return captor.getValue().getFirst().toShip();
    }
}
