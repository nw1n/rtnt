package com.example.rtnt.game.island.web;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.service.IslandService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/islands")
public class IslandController {
    private final IslandService islandService;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                            *
     *                                                                         *
     **************************************************************************/

    public IslandController(IslandService islandService) {
        this.islandService = islandService;
    }

    /***************************************************************************
     *                                                                         *
     * Endpoints                                                               *
     *                                                                         *
     **************************************************************************/

    @GetMapping
    public List<IslandDto> getAll() {
        Map<String, Long> populationByIslandId = this.islandService.listStatuses().stream()
                .collect(Collectors.toMap(IslandStatus::islandId, IslandStatus::population, (left, right) -> left));
        return this.islandService.list().stream()
                .map(island -> IslandDto.from(island, populationByIslandId.getOrDefault(island.id(), 0L)))
                .toList();
    }

    @PostMapping("/recreate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recreate() {
        this.islandService.recreateAll();
    }


    /***************************************************************************
     *                                                                         *
     * DTOs                                                                    *
     *                                                                         *
     **************************************************************************/

    public record IslandDto(String id, String name, int x, int y, int width, int length, long population) {
        static IslandDto from(Island island, long population) {
            return new IslandDto(
                    island.id(),
                    island.name(),
                    island.footprint().x(),
                    island.footprint().y(),
                    island.footprint().width(),
                    island.footprint().length(),
                    population
            );
        }
    }
}
