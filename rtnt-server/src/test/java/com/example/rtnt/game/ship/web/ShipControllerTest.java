package com.example.rtnt.game.ship.web;

import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.service.IslandService;
import com.example.rtnt.game.ship.domain.Ship;
import com.example.rtnt.game.ship.service.ShipService;
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

@WebMvcTest(ShipController.class)
class ShipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipService shipService;

    @MockitoBean
    private IslandService islandService;

    @Test
    void getAllReturnsShipAndIslandName() throws Exception {
        Island island = Island.create("Jamaica", new Footprint(10, 20, 60, 40));
        Ship ship = Ship.create("Black Pearl", island.id(), null);
        when(this.islandService.list()).thenReturn(List.of(island));
        when(this.shipService.list()).thenReturn(List.of(ship));

        this.mockMvc.perform(get("/api/ships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Black Pearl"))
                .andExpect(jsonPath("$[0].islandId").value(island.id()))
                .andExpect(jsonPath("$[0].islandName").value("Jamaica"))
                .andExpect(jsonPath("$[0].speed").value(20))
                .andExpect(jsonPath("$[0].cargoCapacity").value(100));
    }
}
