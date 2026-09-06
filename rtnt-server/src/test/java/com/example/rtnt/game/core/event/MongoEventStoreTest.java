package com.example.rtnt.game.core.event;

import com.example.rtnt.RtntDataTest;
import com.example.rtnt.game.core.event.persistence.EventMongoRepository;
import com.example.rtnt.game.core.event.persistence.MongoEventStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RtntDataTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MongoEventStoreTest {

    @Autowired
    private MongoEventStore eventStore;

    @Autowired
    private EventMongoRepository eventMongoRepository;

    @Test
    void appendsReadsAndTruncatesByTick() {
        this.eventMongoRepository.deleteAll();

        this.eventStore.append(List.of(
                new IslandCreated(0, "i1", "North", 1, 2, 10, 12),
                new IslandPopulationGrew(100, "i1", 3),
                new IslandPopulationGrew(200, "i1", 2)
        ));

        assertEquals(3, this.eventStore.readAll().size());
        assertEquals(2, this.eventStore.readUpToTick(100).size());

        this.eventStore.deleteAfterTick(100);

        List<WorldEvent> remaining = this.eventStore.readAll();
        assertEquals(2, remaining.size());
        assertTrue(remaining.getLast() instanceof IslandPopulationGrew);
        assertEquals(100, remaining.getLast().tick());
    }
}
