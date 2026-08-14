package com.example.rtnt.web;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.service.IslandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
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
    void getAllReturnsIslands() throws Exception {
        Island island = Island.create(
                "Jamaica",
                Footprint.create(10, 20, 60, 40),
                IslandService.defaultInventory(),
                IslandService.defaultTradePrices()
        );
        when(this.islandService.list()).thenReturn(List.of(island));

        this.mockMvc.perform(get("/api/islands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jamaica"))
                .andExpect(jsonPath("$[0].footprint.x").value(10))
                .andExpect(jsonPath("$[0].inventory.gold").value(100))
                .andExpect(jsonPath("$[0].tradePrices.rum").value(3));
    }

    @Test
    void createSavesIsland() throws Exception {
        Island island = Island.create(
                "Cuba",
                Footprint.create(1, 2, 30, 30),
                IslandService.defaultInventory(),
                IslandService.defaultTradePrices()
        );
        when(this.islandService.createAndSave(isNull())).thenReturn(island);

        this.mockMvc.perform(post("/api/islands").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cuba"));
    }
}
