package com.example.rtnt.game.log.web;

import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.log.service.GameLogService;
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

@WebMvcTest(GameLogController.class)
class GameLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameLogService gameLogService;

    @Test
    void getReturnsRecentLogs() throws Exception {
        when(this.gameLogService.listRecent()).thenReturn(List.of(
                GameLogEvent.forTick(2),
                GameLogEvent.forTick(1)
        ));

        this.mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tick").value(2))
                .andExpect(jsonPath("$[0].type").value("TICK"))
                .andExpect(jsonPath("$[0].detail").value("Tick 2"))
                .andExpect(jsonPath("$[1].tick").value(1));
    }
}
