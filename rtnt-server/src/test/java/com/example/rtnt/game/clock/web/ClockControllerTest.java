package com.example.rtnt.game.clock.web;

import com.example.rtnt.game.clock.domain.ClockMode;
import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.clock.service.ClockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClockController.class)
class ClockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClockService clockService;

    @Test
    void getReturnsClock() throws Exception {
        when(this.clockService.get()).thenReturn(new GameClock(4, ClockMode.LIVE, true));

        this.mockMvc.perform(get("/api/clock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tick").value(4))
                .andExpect(jsonPath("$.mode").value("LIVE"))
                .andExpect(jsonPath("$.paused").value(true));
    }

    @Test
    void pauseDelegatesToService() throws Exception {
        when(this.clockService.pause()).thenReturn(GameClock.initial().pause());

        this.mockMvc.perform(post("/api/clock/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paused").value(true));
        verify(this.clockService).pause();
    }

    @Test
    void setModeDelegatesToService() throws Exception {
        when(this.clockService.setMode(ClockMode.BATCH))
                .thenReturn(GameClock.initial().withMode(ClockMode.BATCH));

        this.mockMvc.perform(post("/api/clock/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"BATCH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("BATCH"));
        verify(this.clockService).setMode(ClockMode.BATCH);
    }

    @Test
    void advanceDelegatesToService() throws Exception {
        when(this.clockService.advance(25)).thenReturn(new GameClock(25, ClockMode.BATCH, false));

        this.mockMvc.perform(post("/api/clock/advance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticks\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tick").value(25));
        verify(this.clockService).advance(25);
    }

    @Test
    void advanceRejectsZeroTicks() throws Exception {
        this.mockMvc.perform(post("/api/clock/advance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticks\":0}"))
                .andExpect(status().isBadRequest());
    }
}
