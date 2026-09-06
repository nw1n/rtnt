package com.example.rtnt.game.core.event.web;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.weather.TemperatureChanged;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    public record WorldEventDto(long tick, String type, int delta) {
        static WorldEventDto from(TemperatureChanged event) {
            return new WorldEventDto(event.tick(), TemperatureChanged.TYPE, event.delta());
        }
    }
}
