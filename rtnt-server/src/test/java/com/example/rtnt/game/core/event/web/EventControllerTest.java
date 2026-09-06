package com.example.rtnt.game.core.event.web;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.weather.TemperatureChanged;
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
    void getAllReturnsTemperatureChanges() throws Exception {
        when(this.eventStore.readAll()).thenReturn(List.of(new TemperatureChanged(10, -2)));

        this.mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tick").value(10))
                .andExpect(jsonPath("$[0].type").value("TemperatureChanged"))
                .andExpect(jsonPath("$[0].delta").value(-2));
    }
}
