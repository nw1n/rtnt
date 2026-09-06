package com.example.rtnt.game.core.event;

import com.example.rtnt.RtntDataTest;
import com.example.rtnt.game.island.service.IslandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RtntDataTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventSourcingIT {

    @Autowired
    private EventStore eventStore;

    @Autowired
    private IslandService islandService;

    @Test
    void seedWritesIslandCreatedEvents() {
        List<WorldEvent> events = this.eventStore.readAll();
        assertFalse(events.isEmpty());
        assertTrue(events.stream().allMatch(IslandCreated.class::isInstance));
        assertEquals(this.islandService.list().size(), events.size());
    }

    @Test
    void recreateAppendsWorldClearedAndNewIslands() {
        int before = this.eventStore.readAll().size();

        this.islandService.recreateAll();

        List<WorldEvent> events = this.eventStore.readAll();
        assertInstanceOf(WorldCleared.class, events.get(before));
        long createdAfterClear = events.stream()
                .skip(before + 1)
                .filter(IslandCreated.class::isInstance)
                .count();
        assertEquals(this.islandService.list().size(), createdAfterClear);
    }
}
