package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.usecase.ship.ListJourneysUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JourneyController.class)
class JourneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListJourneysUseCase listJourneysUseCase;

    @Test
    void getAllReturnsSerializedJourneys() throws Exception {
        Journey a = new Journey(
                "journey-1",
                "ship-1",
                "island-a",
                "island-b",
                Instant.parse("2026-03-21T10:00:00Z"),
                null,
                Instant.parse("2026-03-21T10:05:00Z"),
                true
        );
        Journey b = new Journey(
                "journey-2",
                "ship-2",
                "island-c",
                "island-d",
                Instant.parse("2026-03-21T11:00:00Z"),
                Instant.parse("2026-03-21T11:07:00Z"),
                Instant.parse("2026-03-21T11:07:00Z"),
                false
        );
        when(listJourneysUseCase.getLatestJourneys()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/journeys"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("journey-1"))
                .andExpect(jsonPath("$[0].shipId").value("ship-1"))
                .andExpect(jsonPath("$[0].startIslandId").value("island-a"))
                .andExpect(jsonPath("$[0].targetIslandId").value("island-b"))
                .andExpect(jsonPath("$[0].departed").value("2026-03-21T10:00:00Z"))
                .andExpect(jsonPath("$[0].arrived").value(nullValue()))
                .andExpect(jsonPath("$[0].estimatedArrival").value("2026-03-21T10:05:00Z"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].active").value(false));

        verify(listJourneysUseCase, times(1)).getLatestJourneys();
    }

    @Test
    void getAllWithShipIdFilterDelegatesToUseCase() throws Exception {
        Journey journey = new Journey("journey-7", "ship-7", "island-a", "island-b", null, null, null, false);
        when(listJourneysUseCase.getJourneysForShip("ship-7")).thenReturn(List.of(journey));

        mockMvc.perform(get("/api/journeys").param("shipId", "ship-7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shipId").value("ship-7"));

        verify(listJourneysUseCase, times(1)).getJourneysForShip("ship-7");
    }
}
