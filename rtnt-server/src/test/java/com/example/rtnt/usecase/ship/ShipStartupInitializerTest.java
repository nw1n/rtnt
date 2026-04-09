package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import com.example.rtnt.domain.ship.StandardShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipStartupInitializerTest {

    @Mock
    private ShipRepository shipRepository;

    @Mock
    private ShipFactory shipFactory;

    private ShipStartupInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new ShipStartupInitializer(shipRepository, shipFactory, 15);
    }

    @Test
    void runSeedsWhenStoreEmpty() {
        when(shipRepository.count()).thenReturn(0L);
        when(shipFactory.createWithCaribbeanName()).thenReturn(
                StandardShip.create("TestShip", null, null, null, Inventory.empty())
        );

        initializer.run();

        verify(shipRepository, times(1)).count();
        verify(shipFactory, times(15)).createWithCaribbeanName();
        verify(shipRepository, times(15)).save(any(Ship.class));
        verify(shipRepository, never()).deleteAll();
    }

    @Test
    void runSkipsSeedWhenStoreNotEmpty() {
        when(shipRepository.count()).thenReturn(5L);

        initializer.run();

        verify(shipRepository, times(1)).count();
        verify(shipRepository, never()).deleteAll();
        verify(shipRepository, never()).save(any());
        verify(shipFactory, never()).createWithCaribbeanName();
    }
}
