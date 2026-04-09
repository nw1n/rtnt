package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import com.example.rtnt.usecase.island.ListIslandsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IslandController.class)
class IslandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListIslandsUseCase listIslandsUseCase;

    @Test
    void getAllReturnsSerializedIslands() throws Exception {
        // Given
        Island islandA = StandardIsland.fromDocument(
                "id-1",
                "Jamaica",
                Footprint.create(0, 0, 80, 60),
                Inventory.of(Map.of(
                        GoodType.GOLD, 900,
                        GoodType.RUM, 200,
                        GoodType.SUGAR, 180,
                        GoodType.SPICES, 150,
                        GoodType.TOBACCO, 175
                )),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 2,
                        GoodType.SUGAR, 3,
                        GoodType.SPICES, 4,
                        GoodType.TOBACCO, 5
                ))
        );
        Island islandB = StandardIsland.fromDocument(
                "id-2",
                "Cuba",
                Footprint.create(10, 0, 40, 30),
                Inventory.empty(),
                TradePriceList.of(Map.of(
                        GoodType.RUM, 1,
                        GoodType.SUGAR, 1,
                        GoodType.SPICES, 1,
                        GoodType.TOBACCO, 1
                ))
        );
        when(listIslandsUseCase.getAllIslands()).thenReturn(List.of(islandA, islandB));

        // When / Then
        mockMvc.perform(get("/api/islands"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].name").value("Jamaica"))
                .andExpect(jsonPath("$[0].footprint.x").value(0))
                .andExpect(jsonPath("$[0].footprint.y").value(0))
                .andExpect(jsonPath("$[0].footprint.width").value(80))
                .andExpect(jsonPath("$[0].footprint.length").value(60))
                .andExpect(jsonPath("$[0].inventory.gold").value(900))
                .andExpect(jsonPath("$[0].inventory.rum").value(200))
                .andExpect(jsonPath("$[0].inventory.sugar").value(180))
                .andExpect(jsonPath("$[0].inventory.spices").value(150))
                .andExpect(jsonPath("$[0].inventory.tobacco").value(175))
                .andExpect(jsonPath("$[0].tradePrices.rum").value(2))
                .andExpect(jsonPath("$[0].tradePrices.sugar").value(3))
                .andExpect(jsonPath("$[0].tradePrices.spices").value(4))
                .andExpect(jsonPath("$[0].tradePrices.tobacco").value(5))
                .andExpect(jsonPath("$[1].id").value("id-2"))
                .andExpect(jsonPath("$[1].name").value("Cuba"))
                .andExpect(jsonPath("$[1].footprint.x").value(10))
                .andExpect(jsonPath("$[1].footprint.y").value(0))
                .andExpect(jsonPath("$[1].footprint.width").value(40))
                .andExpect(jsonPath("$[1].footprint.length").value(30))
                .andExpect(jsonPath("$[1].inventory.gold").value(0))
                .andExpect(jsonPath("$[1].tradePrices.rum").value(1));

        verify(listIslandsUseCase, times(1)).getAllIslands();
    }
}
