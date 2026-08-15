package com.example.rtnt.service;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.persistence.IslandMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IslandServiceTest {

    @Mock
    private IslandMongoRepository islandMongoRepository;

    @Mock
    private IslandNames islandNames;

    @Test
    void placeKeepsIslandOnTheMap() {
        IslandService islandService = new IslandService(this.islandMongoRepository, this.islandNames, 15);
        Island created = islandService.place("Nassau", List.of());

        assertEquals("Nassau", created.name());
        assertTrue(created.footprint().x() >= 0);
        assertTrue(created.footprint().y() >= 0);
        assertTrue(created.footprint().x() + created.footprint().width() <= 2000);
        assertTrue(created.footprint().y() + created.footprint().length() <= 1000);
    }
}
