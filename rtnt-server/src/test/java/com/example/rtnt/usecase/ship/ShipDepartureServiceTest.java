package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.domain.ship.JourneyRepository;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import com.example.rtnt.domain.ship.StandardShip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipDepartureServiceTest {

    @Mock
    private ShipRepository shipRepository;

    @Mock
    private IslandRepository islandRepository;

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private NpcShipTradingService npcShipTradingService;

    @InjectMocks
    private ShipDepartureService shipDepartureService;

    @Test
    void checkDeparturesLaunchesNonPlayerShipToAnotherIsland() {
        Island islandA = island("island-a", 0, 0, 100, 100);
        Island islandB = island("island-b", 300, 400, 100, 100);
        Ship ship = StandardShip.fromDocument(
                "ship-1",
                "Tempest",
                "island-a",
                null,
                null,
                Inventory.empty()
        );

        when(islandRepository.findAll()).thenReturn(List.of(islandA, islandB));
        when(shipRepository.findAll()).thenReturn(List.of(ship));
        when(shipRepository.findById("ship-1")).thenReturn(java.util.Optional.of(ship));

        shipDepartureService.checkDepartures();

        verify(npcShipTradingService, times(1)).tryNpcBuyOnce("ship-1");

        ArgumentCaptor<Ship> savedCaptor = ArgumentCaptor.forClass(Ship.class);
        verify(shipRepository, times(1)).save(savedCaptor.capture());
        verify(journeyRepository, times(1)).save(org.mockito.ArgumentMatchers.any());

        Ship saved = savedCaptor.getValue();
        assertNull(saved.getIslandId());
        assertNotNull(saved.getJourney());
        assertEquals("island-a", saved.getJourney().startIslandId());
        assertEquals("island-b", saved.getJourney().targetIslandId());

        Instant departed = saved.getJourney().departed();
        Instant estimatedArrival = saved.getJourney().estimatedArrival();
        assertNotNull(departed);
        assertNull(saved.getJourney().arrived());
        assertNotNull(estimatedArrival);

        // Distance between island centers is 500 map units; StandardShip speed is 20 units/second.
        assertEquals(Duration.ofSeconds(25), Duration.between(departed, estimatedArrival));
    }

    @Test
    void checkDeparturesSkipsPlayerControlledAndAtSeaShips() {
        Island islandA = island("island-a", 0, 0, 100, 100);
        Island islandB = island("island-b", 300, 400, 100, 100);
        Ship playerControlled = StandardShip.fromDocument(
                "ship-1",
                "Black Pearl",
                "island-a",
                null,
                "player-1",
                Inventory.empty()
        );
        Ship atSeaNpc = StandardShip.fromDocument(
                "ship-2",
                "Ghost",
                null,
                null,
                null,
                Inventory.empty()
        );

        when(islandRepository.findAll()).thenReturn(List.of(islandA, islandB));
        when(shipRepository.findAll()).thenReturn(List.of(playerControlled, atSeaNpc));

        shipDepartureService.checkDepartures();

        verify(npcShipTradingService, never()).tryNpcBuyOnce(anyString());
        verify(shipRepository, never()).save(org.mockito.ArgumentMatchers.any(Ship.class));
        verify(journeyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void arrivalCheckDocksArrivedShipAtTargetIsland() {
        Island islandA = island("island-a", 0, 0, 100, 100);
        Island islandB = island("island-b", 300, 400, 100, 100);
        when(islandRepository.findAll()).thenReturn(List.of(islandA, islandB));

        Instant departed = Instant.now().minusSeconds(30);
        Instant eta = Instant.now().minusSeconds(1);
        String journeyId = UUID.randomUUID().toString();
        Ship inTransit = StandardShip.fromDocument(
                "ship-3",
                "Windrunner",
                null,
                new com.example.rtnt.domain.ship.Journey(
                        journeyId,
                        "ship-3",
                        "island-a",
                        "island-b",
                        departed,
                        null,
                        eta,
                        true
                ),
                null,
                Inventory.empty()
        );
        when(shipRepository.findAll()).thenReturn(List.of(inTransit));

        ArgumentCaptor<Ship> savedCaptor = ArgumentCaptor.forClass(Ship.class);
        shipDepartureService.arrivalCheck();

        var order = inOrder(shipRepository, npcShipTradingService);
        order.verify(shipRepository).save(savedCaptor.capture());
        order.verify(npcShipTradingService).tryNpcSellOnce("ship-3");

        verify(journeyRepository, times(1)).save(org.mockito.ArgumentMatchers.any());

        Ship arrived = savedCaptor.getValue();
        assertEquals("island-b", arrived.getIslandId());
        assertNotNull(arrived.getJourney());
        assertEquals(journeyId, arrived.getJourney().id());
        assertNotNull(arrived.getJourney().arrived());
    }

    private static Island island(String id, int x, int y, int width, int length) {
        return StandardIsland.fromDocument(
                id,
                id,
                Footprint.create(x, y, width, length),
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );
    }
}
