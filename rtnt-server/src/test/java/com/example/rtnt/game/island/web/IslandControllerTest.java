package com.example.rtnt.game.island.web;

import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.service.IslandService;
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
    void getAllReturnsNameGeographyAndPopulation() throws Exception {
        Island island = Island.create("Jamaica", new Footprint(10, 20, 60, 40));
        when(this.islandService.list()).thenReturn(List.of(island));
        when(this.islandService.listStatuses()).thenReturn(List.of(new IslandStatus(island.id(), 42, Inventory.empty())));

        this.mockMvc.perform(get("/api/islands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jamaica"))
                .andExpect(jsonPath("$[0].x").value(10))
                .andExpect(jsonPath("$[0].y").value(20))
                .andExpect(jsonPath("$[0].width").value(60))
                .andExpect(jsonPath("$[0].length").value(40))
                .andExpect(jsonPath("$[0].population").value(42))
                .andExpect(jsonPath("$[0].inventory.gold").value(0))
                .andExpect(jsonPath("$[0].inventory.rum").value(0))
                .andExpect(jsonPath("$[0].tradePrices.rum").value(3))
                .andExpect(jsonPath("$[0].tradePrices.sugar").value(2))
                .andExpect(jsonPath("$[0].tradePrices.spices").value(4))
                .andExpect(jsonPath("$[0].tradePrices.tobacco").value(5));
    }

    @Test
    void recreateDeletesAndCreatesIslands() throws Exception {
        this.mockMvc.perform(post("/api/islands/recreate"))
                .andExpect(status().isNoContent());
        verify(this.islandService).recreateAll();
    }
}
