package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.usecase.island.ListIslandsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/islands")
public class IslandController {

    private final ListIslandsUseCase listIslandsUseCase;

    public IslandController(ListIslandsUseCase listIslandsUseCase) {
        this.listIslandsUseCase = listIslandsUseCase;
    }

    @GetMapping
    public List<IslandResponse> getAll() {
        return this.listIslandsUseCase.getAllIslands().stream()
                .map(IslandResponse::fromDomain)
                .toList();
    }

    public record IslandResponse(
            String id,
            String name,
            FootprintResponse footprint,
            InventoryResponse inventory,
            TradePricesResponse tradePrices
    ) {
        static IslandResponse fromDomain(Island island) {
            return new IslandResponse(
                    island.getId(),
                    island.getName(),
                    FootprintResponse.fromDomain(island),
                    InventoryResponse.fromDomain(island.getInventory()),
                    TradePricesResponse.fromDomain(island.getTradePrices())
            );
        }
    }

    public record FootprintResponse(int x, int y, int width, int length) {
        static FootprintResponse fromDomain(Island island) {
            return new FootprintResponse(
                    island.getFootprint().getX(),
                    island.getFootprint().getY(),
                    island.getFootprint().getWidth(),
                    island.getFootprint().getLength()
            );
        }
    }

    public record InventoryResponse(int gold, int rum, int sugar, int spices, int tobacco) {
        static InventoryResponse fromDomain(Inventory inventory) {
            Inventory safeInventory = inventory == null ? Inventory.empty() : inventory;
            return new InventoryResponse(
                    safeInventory.getAmount(GoodType.GOLD),
                    safeInventory.getAmount(GoodType.RUM),
                    safeInventory.getAmount(GoodType.SUGAR),
                    safeInventory.getAmount(GoodType.SPICES),
                    safeInventory.getAmount(GoodType.TOBACCO)
            );
        }
    }

    public record TradePricesResponse(int rum, int sugar, int spices, int tobacco) {
        static TradePricesResponse fromDomain(TradePriceList tradePriceList) {
            TradePriceList safeTradePriceList = tradePriceList == null ? TradePriceList.defaultPrices() : tradePriceList;
            return new TradePricesResponse(
                    safeTradePriceList.getPrice(GoodType.RUM),
                    safeTradePriceList.getPrice(GoodType.SUGAR),
                    safeTradePriceList.getPrice(GoodType.SPICES),
                    safeTradePriceList.getPrice(GoodType.TOBACCO)
            );
        }
    }
}
