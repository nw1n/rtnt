package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.player.Player;
import com.example.rtnt.domain.player.PlayerRepository;
import com.example.rtnt.domain.player.StandardPlayer;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.StandardShip;
import com.example.rtnt.usecase.ship.CreatePlayerControlledShipUseCase;
import com.example.rtnt.usecase.ship.ListShipsUseCase;
import com.example.rtnt.usecase.ship.StartPlayerJourneyUseCase;
import com.example.rtnt.usecase.ship.TradeWithIslandUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipController.class)
class ShipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListShipsUseCase listShipsUseCase;

    @MockBean
    private CreatePlayerControlledShipUseCase createPlayerControlledShipUseCase;

    @MockBean
    private TradeWithIslandUseCase tradeWithIslandUseCase;

    @MockBean
    private StartPlayerJourneyUseCase startPlayerJourneyUseCase;

    @MockBean
    private PlayerRepository playerRepository;

    @Test
    void getAllReturnsSerializedShips() throws Exception {
        // Given
        Ship shipA = StandardShip.fromDocument(
                "id-1",
                "Black Pearl",
                "island-1",
                null,
                "player-1",
                Inventory.of(Map.of(
                        GoodType.GOLD, 125,
                        GoodType.RUM, 12,
                        GoodType.SUGAR, 9,
                        GoodType.SPICES, 5,
                        GoodType.TOBACCO, 11
                ))
        );
        Ship shipB = StandardShip.fromDocument("id-2", "Flying Dutchman", null, null, null, Inventory.empty());
        when(listShipsUseCase.getAllShips()).thenReturn(List.of(shipA, shipB));

        // When / Then
        mockMvc.perform(get("/api/ships"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].name").value("Black Pearl"))
                .andExpect(jsonPath("$[0].islandId").value("island-1"))
                .andExpect(jsonPath("$[0].journey").value(nullValue()))
                .andExpect(jsonPath("$[0].playerId").value("player-1"))
                .andExpect(jsonPath("$[0].inventory.gold").value(125))
                .andExpect(jsonPath("$[0].inventory.rum").value(12))
                .andExpect(jsonPath("$[0].inventory.sugar").value(9))
                .andExpect(jsonPath("$[0].inventory.spices").value(5))
                .andExpect(jsonPath("$[0].inventory.tobacco").value(11))
                .andExpect(jsonPath("$[1].id").value("id-2"))
                .andExpect(jsonPath("$[1].name").value("Flying Dutchman"))
                .andExpect(jsonPath("$[1].islandId").value(nullValue()))
                .andExpect(jsonPath("$[1].playerId").value(nullValue()))
                .andExpect(jsonPath("$[1].inventory.gold").value(0))
                .andExpect(jsonPath("$[1].inventory.rum").value(0))
                .andExpect(jsonPath("$[1].inventory.sugar").value(0))
                .andExpect(jsonPath("$[1].inventory.spices").value(0))
                .andExpect(jsonPath("$[1].inventory.tobacco").value(0));

        verify(listShipsUseCase, times(1)).getAllShips();
    }

    @Test
    void getAllWithIslandIdFilterDelegatesToFilteredUseCase() throws Exception {
        // Given
        Ship ship = StandardShip.fromDocument("id-3", "Interceptor", "island-7", null, null, Inventory.empty());
        when(listShipsUseCase.getShipsAtIsland("island-7")).thenReturn(List.of(ship));

        // When / Then
        mockMvc.perform(get("/api/ships").param("islandId", "island-7"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("id-3"))
                .andExpect(jsonPath("$[0].name").value("Interceptor"))
                .andExpect(jsonPath("$[0].islandId").value("island-7"))
                .andExpect(jsonPath("$[0].playerId").value(nullValue()))
                .andExpect(jsonPath("$[0].inventory.gold").value(0));

        verify(listShipsUseCase, times(1)).getShipsAtIsland("island-7");
    }

    @Test
    void buyFromIslandReturnsUpdatedShip() throws Exception {
        Ship updated = StandardShip.fromDocument(
                "id-9",
                "Black Pearl",
                "island-1",
                null,
                "player-9",
                Inventory.of(Map.of(
                        GoodType.GOLD, 90,
                        GoodType.RUM, 10,
                        GoodType.SUGAR, 0,
                        GoodType.SPICES, 0,
                        GoodType.TOBACCO, 0
                ))
        );
        when(tradeWithIslandUseCase.buyFromIsland("id-9", GoodType.RUM, 10)).thenReturn(updated);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/ships/id-9/trade/buy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"goodType\":\"RUM\",\"amount\":10}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-9"))
                .andExpect(jsonPath("$.playerId").value("player-9"))
                .andExpect(jsonPath("$.inventory.gold").value(90))
                .andExpect(jsonPath("$.inventory.rum").value(10));

        verify(tradeWithIslandUseCase, times(1)).buyFromIsland("id-9", GoodType.RUM, 10);
    }

    @Test
    void sellToIslandReturnsUpdatedShip() throws Exception {
        Ship updated = StandardShip.fromDocument(
                "id-10",
                "Interceptor",
                "island-1",
                null,
                "player-10",
                Inventory.of(Map.of(
                        GoodType.GOLD, 120,
                        GoodType.RUM, 2,
                        GoodType.SUGAR, 0,
                        GoodType.SPICES, 0,
                        GoodType.TOBACCO, 0
                ))
        );
        when(tradeWithIslandUseCase.sellToIsland("id-10", GoodType.RUM, 3)).thenReturn(updated);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/ships/id-10/trade/sell")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"goodType\":\"RUM\",\"amount\":3}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-10"))
                .andExpect(jsonPath("$.playerId").value("player-10"))
                .andExpect(jsonPath("$.inventory.gold").value(120))
                .andExpect(jsonPath("$.inventory.rum").value(2));

        verify(tradeWithIslandUseCase, times(1)).sellToIsland("id-10", GoodType.RUM, 3);
    }

    @Test
    void getAllWithPlayerIdFilterDelegatesToPlayerUseCase() throws Exception {
        Ship ship = StandardShip.fromDocument("id-11", "Tempest", null, null, "player-7", Inventory.empty());
        when(listShipsUseCase.getShipsForPlayer("player-7")).thenReturn(List.of(ship));

        mockMvc.perform(get("/api/ships").param("playerId", "player-7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("id-11"))
                .andExpect(jsonPath("$[0].playerId").value("player-7"));

        verify(listShipsUseCase, times(1)).getShipsForPlayer("player-7");
    }

    @Test
    void createPlayerControlledShipReturnsCreatedShip() throws Exception {
        Player createdPlayer = StandardPlayer.fromDocument("player-12", "Jack Sparrow", "#ABCDEF");
        Ship created = StandardShip.fromDocument("id-12", "Poseidon", null, null, "player-12", Inventory.empty());
        when(createPlayerControlledShipUseCase.create("Poseidon", "Jack Sparrow"))
                .thenReturn(new CreatePlayerControlledShipUseCase.CreationResult(createdPlayer, created));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/ships/player-controlled")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"shipName\":\"Poseidon\",\"playerName\":\"Jack Sparrow\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ship.id").value("id-12"))
                .andExpect(jsonPath("$.ship.name").value("Poseidon"))
                .andExpect(jsonPath("$.ship.playerId").value("player-12"))
                .andExpect(jsonPath("$.ship.playerHexColor").value("#ABCDEF"))
                .andExpect(jsonPath("$.player.id").value("player-12"))
                .andExpect(jsonPath("$.player.name").value("Jack Sparrow"))
                .andExpect(jsonPath("$.player.hexColor").value("#ABCDEF"));

        verify(createPlayerControlledShipUseCase, times(1)).create("Poseidon", "Jack Sparrow");
    }

    @Test
    void createPlayerControlledShipReturnsBadRequestWhenInputIsInvalid() throws Exception {
        when(createPlayerControlledShipUseCase.create("", "Jack Sparrow"))
                .thenThrow(new IllegalArgumentException("Ship name is required"));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/ships/player-controlled")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"shipName\":\"\",\"playerName\":\"Jack Sparrow\"}")
                )
                .andExpect(status().isBadRequest());
    }
}
