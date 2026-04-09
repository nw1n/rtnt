package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import com.example.rtnt.domain.ship.StandardShip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NpcShipTradingServiceTest {

    @Mock
    private ShipRepository shipRepository;

    @Mock
    private IslandRepository islandRepository;

    @Mock
    private TradeWithIslandUseCase tradeWithIslandUseCase;

    @InjectMocks
    private NpcShipTradingService npcShipTradingService;

    @Test
    void maxAffordableBuyAmount_zeroWhenNoGold() {
        Ship ship = shipAtIsland("s1", "i1", Map.of(GoodType.GOLD, 0, GoodType.RUM, 0));
        Island island = islandWithStock(Map.of(GoodType.RUM, 50));

        assertEquals(0, NpcShipTradingService.maxAffordableBuyAmount(ship, island, GoodType.RUM));
    }

    @Test
    void maxAffordableBuyAmount_limitedByCargoIslandStockAndGold() {
        Ship ship = shipAtIsland(
                "s1",
                "i1",
                Map.of(GoodType.GOLD, 100, GoodType.RUM, 0, GoodType.SUGAR, 95)
        );
        Island island = islandWithStock(Map.of(GoodType.SUGAR, 20));

        assertEquals(5, NpcShipTradingService.maxAffordableBuyAmount(ship, island, GoodType.SUGAR));
    }

    @Test
    void maxAffordableSellAmount_limitedByShipStockAndIslandGold() {
        Ship ship = shipAtIsland(
                "s1",
                "i1",
                Map.of(GoodType.GOLD, 0, GoodType.RUM, 10)
        );
        Island island = islandWithStock(Map.of(GoodType.GOLD, 12));

        assertEquals(4, NpcShipTradingService.maxAffordableSellAmount(ship, island, GoodType.RUM));
    }

    @Test
    void tryNpcBuyOnce_skipsPlayerControlledShips() {
        Ship playerShip = StandardShip.fromDocument(
                "ps",
                "Player",
                "i1",
                null,
                "player-1",
                Inventory.of(Map.of(GoodType.GOLD, 10_000, GoodType.RUM, 50))
        );
        when(shipRepository.findById("ps")).thenReturn(Optional.of(playerShip));

        npcShipTradingService.tryNpcBuyOnce("ps");

        verify(tradeWithIslandUseCase, never()).buyFromIsland(any(), any(), anyInt());
        verify(tradeWithIslandUseCase, never()).sellToIsland(any(), any(), anyInt());
    }

    @Test
    void tryNpcBuyOnce_skipsShipsNotAtIsland() {
        Ship atSea = StandardShip.fromDocument("s1", "Ghost", null, null, null, Inventory.empty());
        when(shipRepository.findById("s1")).thenReturn(Optional.of(atSea));

        npcShipTradingService.tryNpcBuyOnce("s1");

        verify(tradeWithIslandUseCase, never()).buyFromIsland(any(), any(), anyInt());
    }

    @Test
    void tryNpcSellOnce_skipsPlayerControlledShips() {
        Ship playerShip = StandardShip.fromDocument(
                "ps",
                "Player",
                "i1",
                null,
                "player-1",
                Inventory.of(Map.of(GoodType.RUM, 5))
        );
        when(shipRepository.findById("ps")).thenReturn(Optional.of(playerShip));

        npcShipTradingService.tryNpcSellOnce("ps");

        verify(tradeWithIslandUseCase, never()).sellToIsland(any(), any(), anyInt());
    }

    private static Ship shipAtIsland(String id, String islandId, Map<GoodType, Integer> amounts) {
        return StandardShip.fromDocument(id, "NPC", islandId, null, null, Inventory.of(amounts));
    }

    private static Island islandWithStock(Map<GoodType, Integer> amounts) {
        return StandardIsland.fromDocument(
                "i1",
                "Tortuga",
                Footprint.create(0, 0, 50, 50),
                Inventory.of(amounts),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 3,
                        GoodType.SUGAR, 2,
                        GoodType.SPICES, 4,
                        GoodType.TOBACCO, 5
                ))
        );
    }
}
