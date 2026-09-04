package com.example.rtnt.game.trade.web;

import com.example.rtnt.game.inventory.domain.GoodType;
import com.example.rtnt.game.trade.domain.TradeEvent;
import com.example.rtnt.game.trade.domain.TradeType;
import com.example.rtnt.game.trade.service.TradeEventStore;
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

@WebMvcTest(TradeEventController.class)
class TradeEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeEventStore tradeEventStore;

    @Test
    void getAllReturnsEvents() throws Exception {
        TradeEvent event = TradeEvent.create(
                12,
                TradeType.SELL_TO_ISLAND,
                "s1",
                "Pearl",
                "i1",
                "Jamaica",
                GoodType.SUGAR,
                4,
                2,
                8
        );
        when(this.tradeEventStore.list()).thenReturn(List.of(event));

        this.mockMvc.perform(get("/api/trade-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tick").value(12))
                .andExpect(jsonPath("$[0].tradeType").value("SELL_TO_ISLAND"))
                .andExpect(jsonPath("$[0].shipName").value("Pearl"))
                .andExpect(jsonPath("$[0].islandName").value("Jamaica"))
                .andExpect(jsonPath("$[0].goodType").value("SUGAR"))
                .andExpect(jsonPath("$[0].amount").value(4))
                .andExpect(jsonPath("$[0].unitPrice").value(2))
                .andExpect(jsonPath("$[0].totalPrice").value(8));
    }
}
