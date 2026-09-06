package com.example.rtnt.game.weather.web;

import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.weather.Weather;
import com.example.rtnt.game.weather.WeatherSample;
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

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameLoop gameLoop;

    @Test
    void getReturnsTemperature() throws Exception {
        when(this.gameLoop.weather()).thenReturn(new Weather(17));

        this.mockMvc.perform(get("/api/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(17));
    }

    @Test
    void historyReturnsFoldedSamples() throws Exception {
        when(this.gameLoop.weatherHistory()).thenReturn(List.of(
                new WeatherSample(0, 20),
                new WeatherSample(10, 22)
        ));

        this.mockMvc.perform(get("/api/weather/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tick").value(0))
                .andExpect(jsonPath("$[0].temperature").value(20))
                .andExpect(jsonPath("$[1].tick").value(10))
                .andExpect(jsonPath("$[1].temperature").value(22));
    }
}
