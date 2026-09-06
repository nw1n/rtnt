package com.example.rtnt.game.core.event.web;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.core.event.IslandCreated;
import com.example.rtnt.game.core.event.IslandPopulationGrew;
import com.example.rtnt.game.core.event.WorldCleared;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventStore eventStore;

    @Test
    void getAllReturnsRecordedFacts() throws Exception {
        when(this.eventStore.readAll()).thenReturn(List.of(
                new IslandCreated(0, "i1", "North", 1, 2, 10, 12),
                new IslandPopulationGrew(100, "i1", 2),
                new WorldCleared(200)
        ));

        this.mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("IslandCreated"))
                .andExpect(jsonPath("$[0].tick").value(0))
                .andExpect(jsonPath("$[0].payload.name").value("North"))
                .andExpect(jsonPath("$[1].type").value("IslandPopulationGrew"))
                .andExpect(jsonPath("$[1].payload.amount").value(2))
                .andExpect(jsonPath("$[2].type").value("WorldCleared"));
    }
}
