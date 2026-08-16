package com.example.rtnt.web;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.service.IslandService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/islands")
public class IslandController {
    private final IslandService islandService;

    public IslandController(IslandService islandService) {
        this.islandService = islandService;
    }

    @GetMapping
    public List<IslandResponse> getAll() {
        return this.islandService.list().stream()
                .map(IslandResponse::from)
                .toList();
    }

    @PostMapping("/recreate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recreate() {
        this.islandService.recreateAll();
    }

    public record IslandResponse(String id, String name, int x, int y, int width, int length) {
        static IslandResponse from(Island island) {
            return new IslandResponse(
                    island.id(),
                    island.name(),
                    island.footprint().x(),
                    island.footprint().y(),
                    island.footprint().width(),
                    island.footprint().length()
            );
        }
    }
}
