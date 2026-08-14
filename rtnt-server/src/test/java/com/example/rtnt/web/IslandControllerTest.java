package com.example.rtnt.web;

import com.example.rtnt.domain.Footprint;
import com.example.rtnt.domain.Island;
import com.example.rtnt.service.IslandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IslandController.class)
class IslandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IslandService islandService;

    @Test
    void getAllReturnsNameAndGeography() throws Exception {
        Island island = Island.create("Jamaica", Footprint.create(10, 20, 60, 40));
        when(this.islandService.list()).thenReturn(List.of(island));

        this.mockMvc.perform(get("/api/islands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jamaica"))
                .andExpect(jsonPath("$[0].x").value(10))
                .andExpect(jsonPath("$[0].y").value(20))
                .andExpect(jsonPath("$[0].width").value(60))
                .andExpect(jsonPath("$[0].length").value(40))
                .andExpect(jsonPath("$[0].inventory").doesNotExist());
    }

    @Test
    void recreateDeletesAndCreatesIslands() throws Exception {
        Island island = Island.create("Jamaica", Footprint.create(10, 20, 60, 40));
        when(this.islandService.recreateAll()).thenReturn(List.of(island));

        this.mockMvc.perform(post("/api/islands/recreate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jamaica"));
        verify(this.islandService).recreateAll();
    }
}
