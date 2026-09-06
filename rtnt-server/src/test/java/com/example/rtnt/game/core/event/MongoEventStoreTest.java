package com.example.rtnt.game.core.event;

import com.example.rtnt.RtntDataTest;
import com.example.rtnt.game.core.event.persistence.MongoEventStore;
import com.example.rtnt.game.weather.TemperatureChanged;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RtntDataTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MongoEventStoreTest {

    @Autowired
    private MongoEventStore eventStore;

    @Test
    void appendsAndReadsTemperatureChanges() {
        this.eventStore.append(new TemperatureChanged(10, 2));
        this.eventStore.append(new TemperatureChanged(20, -1));

        List<TemperatureChanged> events = this.eventStore.readAll();

        assertEquals(2, events.size());
        assertEquals(10, events.getFirst().tick());
        assertEquals(2, events.getFirst().delta());
        assertEquals(-1, events.getLast().delta());
    }
}
