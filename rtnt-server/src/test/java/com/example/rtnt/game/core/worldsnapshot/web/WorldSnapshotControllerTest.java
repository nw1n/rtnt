package com.example.rtnt.game.core.worldsnapshot.web;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.inventory.domain.Inventory;
import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.ship.domain.Ship;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

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
    void getAllReturnsTickIslandInventoriesAndShips() throws Exception {
        Island island = Island.create("Jamaica", new Footprint(10, 20, 60, 40));
        IslandStatus status = new IslandStatus(
                island.id(),
                12,
                Inventory.of(Map.of(GoodType.GOLD, 40, GoodType.RUM, 7))
        );
        Ship ship = Ship.create(
                "Black Pearl",
                island.id(),
                null,
                null,
                Inventory.of(Map.of(GoodType.GOLD, 200, GoodType.SUGAR, 4))
        );
        when(this.worldSnapshotStore.list()).thenReturn(List.of(
                new WorldSnapshot(0, List.of(island), List.of(status), List.of(ship)),
                new WorldSnapshot(
                        1000,
                        List.of(island),
                        List.of(new IslandStatus(island.id(), 18, Inventory.empty())),
                        List.of()
                )
        ));

        this.mockMvc.perform(get("/api/world-snapshots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tick").value(0))
                .andExpect(jsonPath("$[0].islands[0].id").value(island.id()))
                .andExpect(jsonPath("$[0].islands[0].name").value("Jamaica"))
                .andExpect(jsonPath("$[0].islands[0].population").value(12))
                .andExpect(jsonPath("$[0].islands[0].inventory.gold").value(40))
                .andExpect(jsonPath("$[0].islands[0].inventory.rum").value(7))
                .andExpect(jsonPath("$[0].islands[0].tradePrices.rum").value(3))
                .andExpect(jsonPath("$[0].ships[0].name").value("Black Pearl"))
                .andExpect(jsonPath("$[0].ships[0].inventory.gold").value(200))
                .andExpect(jsonPath("$[0].ships[0].inventory.sugar").value(4))
                .andExpect(jsonPath("$[1].tick").value(1000))
                .andExpect(jsonPath("$[1].islands[0].population").value(18))
                .andExpect(jsonPath("$[1].ships").isEmpty());
    }
}
