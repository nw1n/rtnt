package com.example.rtnt.web;

import com.example.rtnt.domain.Island;
import com.example.rtnt.service.IslandService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping
    public IslandResponse create(@RequestBody(required = false) CreateIslandRequest request) {
        try {
            String name = request == null ? null : request.name();
            return IslandResponse.from(this.islandService.createAndSave(name));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    public record CreateIslandRequest(String name) {
    }

    public record IslandResponse(String id, String name, int x, int y, int width, int length) {
        static IslandResponse from(Island island) {
            return new IslandResponse(
                    island.getId(),
                    island.getName(),
                    island.getFootprint().getX(),
                    island.getFootprint().getY(),
                    island.getFootprint().getWidth(),
                    island.getFootprint().getLength()
            );
        }
    }
}
