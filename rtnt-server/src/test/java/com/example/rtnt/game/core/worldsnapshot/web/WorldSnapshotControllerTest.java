package com.example.rtnt.game.core.worldsnapshot.web;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
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

@WebMvcTest(WorldSnapshotController.class)
class WorldSnapshotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorldSnapshotStore worldSnapshotStore;

    @Test
    void getAllReturnsTickAndIslandPopulations() throws Exception {
        Island island = Island.create("Jamaica", new Footprint(10, 20, 60, 40));
        when(this.worldSnapshotStore.list()).thenReturn(List.of(
                new WorldSnapshot(0, List.of(island), List.of(new IslandStatus(island.id(), 12))),
                new WorldSnapshot(1000, List.of(island), List.of(new IslandStatus(island.id(), 18)))
        ));

        this.mockMvc.perform(get("/api/world-snapshots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tick").value(0))
                .andExpect(jsonPath("$[0].islands[0].id").value(island.id()))
                .andExpect(jsonPath("$[0].islands[0].name").value("Jamaica"))
                .andExpect(jsonPath("$[0].islands[0].population").value(12))
                .andExpect(jsonPath("$[1].tick").value(1000))
                .andExpect(jsonPath("$[1].islands[0].population").value(18));
    }
}
