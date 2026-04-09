package com.example.rtnt.usecase.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Use-case service for retrieving all islands.
 */
@Component
public class ListIslandsUseCase {

    private final IslandRepository islandRepository;

    public ListIslandsUseCase(IslandRepository islandRepository) {
        this.islandRepository = islandRepository;
    }

    public List<Island> getAllIslands() {
        return this.islandRepository.findAll();
    }
}
