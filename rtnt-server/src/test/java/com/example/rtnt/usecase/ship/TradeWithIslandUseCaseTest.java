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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeWithIslandUseCaseTest {

    @Mock
    private ShipRepository shipRepository;

    @Mock
    private IslandRepository islandRepository;

    @Mock
    private TradeEventPublisher tradeEventPublisher;

    @InjectMocks
    private TradeWithIslandUseCase tradeWithIslandUseCase;

    private Ship ship;
    private Island island;

    @BeforeEach
    void setUp() {
        ship = StandardShip.fromDocument(
                "ship-1",
                "Black Pearl",
                "island-1",
                null,
                null,
                Inventory.of(Map.of(
                        GoodType.GOLD, 100,
                        GoodType.RUM, 0,
                        GoodType.SUGAR, 0,
                        GoodType.SPICES, 0,
                        GoodType.TOBACCO, 0
                ))
        );

        island = StandardIsland.fromDocument(
                "island-1",
                "Tortuga",
                Footprint.create(10, 10, 60, 60),
                Inventory.of(Map.of(
                        GoodType.GOLD, 500,
                        GoodType.RUM, 20,
                        GoodType.SUGAR, 20,
                        GoodType.SPICES, 20,
                        GoodType.TOBACCO, 20
                )),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 3,
                        GoodType.SUGAR, 2,
                        GoodType.SPICES, 4,
                        GoodType.TOBACCO, 5
                ))
        );
    }

    @Test
    void buyFromIslandTransfersGoodsAndGold() {
        when(shipRepository.findById("ship-1")).thenReturn(Optional.of(ship));
        when(islandRepository.findById("island-1")).thenReturn(Optional.of(island));
        when(shipRepository.save(ship)).thenReturn(ship);

        Ship result = tradeWithIslandUseCase.buyFromIsland("ship-1", GoodType.RUM, 5);

        assertEquals(85, result.getInventory().getAmount(GoodType.GOLD));
        assertEquals(5, result.getInventory().getAmount(GoodType.RUM));
        assertEquals(515, island.getInventory().getAmount(GoodType.GOLD));
        assertEquals(15, island.getInventory().getAmount(GoodType.RUM));
        verify(islandRepository, times(1)).save(island);
        verify(shipRepository, times(1)).save(ship);
        verify(tradeEventPublisher, timeout(1000).times(1)).publish(org.mockito.ArgumentMatchers.any(TradeEvent.class));
    }

    @Test
    void sellToIslandTransfersGoodsAndGold() {
        ship.getInventory().setAmount(GoodType.RUM, 7);
        when(shipRepository.findById("ship-1")).thenReturn(Optional.of(ship));
        when(islandRepository.findById("island-1")).thenReturn(Optional.of(island));
        when(shipRepository.save(ship)).thenReturn(ship);

        Ship result = tradeWithIslandUseCase.sellToIsland("ship-1", GoodType.RUM, 3);

        assertEquals(109, result.getInventory().getAmount(GoodType.GOLD));
        assertEquals(4, result.getInventory().getAmount(GoodType.RUM));
        assertEquals(491, island.getInventory().getAmount(GoodType.GOLD));
        assertEquals(23, island.getInventory().getAmount(GoodType.RUM));
        verify(islandRepository, times(1)).save(island);
        verify(shipRepository, times(1)).save(ship);
        verify(tradeEventPublisher, timeout(1000).times(1)).publish(org.mockito.ArgumentMatchers.any(TradeEvent.class));
    }

    @Test
    void buyFailsWhenShipNotAnchored() {
        Ship atSeaShip = StandardShip.fromDocument("ship-2", "Interceptor", null, null, null, Inventory.empty());
        when(shipRepository.findById("ship-2")).thenReturn(Optional.of(atSeaShip));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeWithIslandUseCase.buyFromIsland("ship-2", GoodType.RUM, 1)
        );

        assertEquals("Ship must be anchored at an island to trade", exception.getMessage());
        verify(islandRepository, never()).save(island);
    }

    @Test
    void buyFailsWhenShipNotFound() {
        when(shipRepository.findById("missing-ship")).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> tradeWithIslandUseCase.buyFromIsland("missing-ship", GoodType.RUM, 1)
        );

        assertEquals("Ship not found: missing-ship", exception.getMessage());
    }

    @Test
    void buyRejectsTradingGoldAsCargoGood() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeWithIslandUseCase.buyFromIsland("ship-1", GoodType.GOLD, 1)
        );

        assertEquals("Cannot trade GOLD as a cargo good", exception.getMessage());
    }

    @Test
    void buyFailsWhenHoldCannotFitPurchase() {
        ship.getInventory().setAmount(GoodType.RUM, 98);
        when(shipRepository.findById("ship-1")).thenReturn(Optional.of(ship));
        when(islandRepository.findById("island-1")).thenReturn(Optional.of(island));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeWithIslandUseCase.buyFromIsland("ship-1", GoodType.RUM, 5)
        );

        assertEquals(
                "Cannot buy 5 units; ship hold fits 2 more (capacity 100)",
                exception.getMessage()
        );
        verify(islandRepository, never()).save(island);
        verify(shipRepository, never()).save(ship);
    }
}
