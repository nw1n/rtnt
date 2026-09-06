package com.example.rtnt.game.core.event.web;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.core.event.IslandCreated;
import com.example.rtnt.game.core.event.IslandPopulationGrew;
import com.example.rtnt.game.core.event.WorldCleared;
import com.example.rtnt.game.core.event.WorldEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventStore eventStore;

    public EventController(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @GetMapping
    public List<WorldEventDto> getAll() {
        return this.eventStore.readAll().stream()
                .map(WorldEventDto::from)
                .toList();
    }

    public record WorldEventDto(long tick, String type, Map<String, Object> payload) {
        static WorldEventDto from(WorldEvent event) {
            return new WorldEventDto(event.tick(), event.type(), payload(event));
        }

        private static Map<String, Object> payload(WorldEvent event) {
            Map<String, Object> payload = new LinkedHashMap<>();
            switch (event) {
                case IslandCreated created -> {
                    payload.put("islandId", created.islandId());
                    payload.put("name", created.name());
                    payload.put("x", created.x());
                    payload.put("y", created.y());
                    payload.put("width", created.width());
                    payload.put("length", created.length());
                }
                case IslandPopulationGrew grew -> {
                    payload.put("islandId", grew.islandId());
                    payload.put("amount", grew.amount());
                }
                case WorldCleared ignored -> {
                }
            }
            return payload;
        }
    }
}
