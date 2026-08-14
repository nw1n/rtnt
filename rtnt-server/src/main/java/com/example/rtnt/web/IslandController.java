package com.example.rtnt.web;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.service.IslandService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/islands")
public class IslandController {
    private final IslandService islandService;

    public IslandController(IslandService islandService) {
        this.islandService = islandService;
    }

    @GetMapping
    public List<IslandResponse> getAll() {
        return this.islandService.list().stream()
                .map(IslandResponse::fromDomain)
                .toList();
    }

    @PostMapping
    public IslandResponse create(@RequestBody(required = false) CreateIslandRequest request) {
        try {
            String name = request == null ? null : request.name();
            return IslandResponse.fromDomain(this.islandService.createAndSave(name));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    public record CreateIslandRequest(String name) {
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
            Inventory safe = inventory == null ? Inventory.empty() : inventory;
            return new InventoryResponse(
                    safe.getAmount(GoodType.GOLD),
                    safe.getAmount(GoodType.RUM),
                    safe.getAmount(GoodType.SUGAR),
                    safe.getAmount(GoodType.SPICES),
                    safe.getAmount(GoodType.TOBACCO)
            );
        }
    }

    public record TradePricesResponse(int rum, int sugar, int spices, int tobacco) {
        static TradePricesResponse fromDomain(TradePriceList tradePriceList) {
            TradePriceList safe = tradePriceList == null ? TradePriceList.defaultPrices() : tradePriceList;
            return new TradePricesResponse(
                    safe.getPrice(GoodType.RUM),
                    safe.getPrice(GoodType.SUGAR),
                    safe.getPrice(GoodType.SPICES),
                    safe.getPrice(GoodType.TOBACCO)
            );
        }
    }
}
