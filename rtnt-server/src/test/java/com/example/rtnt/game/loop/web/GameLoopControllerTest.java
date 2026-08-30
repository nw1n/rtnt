package com.example.rtnt.game.loop.web;

import com.example.rtnt.game.core.loop.ClockMode;
import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.core.loop.GameLoopStatus;
import com.example.rtnt.game.core.loop.web.GameLoopController;
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

@WebMvcTest(GameLoopController.class)
class GameLoopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameLoop gameLoop;

    @Test
    void getReturnsStatus() throws Exception {
        when(this.gameLoop.get()).thenReturn(new GameLoopStatus(4, ClockMode.LIVE, true));

        this.mockMvc.perform(get("/api/game-loop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tick").value(4))
                .andExpect(jsonPath("$.mode").value("LIVE"))
                .andExpect(jsonPath("$.paused").value(true));
    }

    @Test
    void pauseDelegatesToGameLoop() throws Exception {
        when(this.gameLoop.pause()).thenReturn(new GameLoopStatus(0, ClockMode.LIVE, true));

        this.mockMvc.perform(post("/api/game-loop/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paused").value(true));
        verify(this.gameLoop).pause();
    }

    @Test
    void setModeDelegatesToGameLoop() throws Exception {
        when(this.gameLoop.setMode(ClockMode.BATCH))
                .thenReturn(new GameLoopStatus(0, ClockMode.BATCH, false));

        this.mockMvc.perform(post("/api/game-loop/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"BATCH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("BATCH"));
        verify(this.gameLoop).setMode(ClockMode.BATCH);
    }

    @Test
    void advanceDelegatesToGameLoop() throws Exception {
        when(this.gameLoop.advance(25)).thenReturn(new GameLoopStatus(25, ClockMode.BATCH, false));

        this.mockMvc.perform(post("/api/game-loop/advance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticks\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tick").value(25));
        verify(this.gameLoop).advance(25);
    }

    @Test
    void advanceRejectsZeroTicks() throws Exception {
        this.mockMvc.perform(post("/api/game-loop/advance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticks\":0}"))
                .andExpect(status().isBadRequest());
    }
}
