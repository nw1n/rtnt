package com.example.rtnt.adapter.out.persistence.island;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB document representation of an Island.
 * This is the persistence model that maps to the domain model.
 */
@Document(collection = "islands")
public class IslandDocument {
    @Id
    private String id;
    private String name;
    private FootprintEmbedded footprint;
    private Map<String, Integer> inventory;
    private Map<String, Integer> tradePrices;

    // Default constructor for Spring Data MongoDB
    public IslandDocument() {
    }

    public IslandDocument(String id, String name, FootprintEmbedded footprint) {
        this(id, name, footprint, null, null);
    }

    public IslandDocument(
            String id,
            String name,
            FootprintEmbedded footprint,
            Map<String, Integer> inventory
    ) {
        this(id, name, footprint, inventory, null);
    }

    public IslandDocument(
            String id,
            String name,
            FootprintEmbedded footprint,
            Map<String, Integer> inventory,
            Map<String, Integer> tradePrices
    ) {
        this.id = id;
        this.name = name;
        this.footprint = footprint;
        this.inventory = inventory;
        this.tradePrices = tradePrices;
    }

    /**
     * Convert from domain model to persistence model.
     */
    public static IslandDocument fromDomain(Island island) {
        if (island == null) {
            throw new IllegalArgumentException("Island cannot be null");
        }
        Footprint domainFootprint = island.getFootprint();
        if (domainFootprint == null) {
            throw new IllegalArgumentException("Island footprint cannot be null");
        }
        FootprintEmbedded footprintEmbedded = new FootprintEmbedded(
                domainFootprint.getX(),
                domainFootprint.getY(),
                domainFootprint.getWidth(),
                domainFootprint.getLength()
        );
        return new IslandDocument(
                island.getId(),
                island.getName(),
                footprintEmbedded,
                toStoredInventory(island.getInventory()),
                toStoredTradePrices(island.getTradePrices())
        );
    }

    /**
     * Convert from persistence model to domain model.
     */
    public Island toDomain() {
        if (this.footprint == null) {
            throw new IllegalStateException("IslandDocument footprint cannot be null");
        }
        Footprint mappedFootprint = Footprint.create(
                this.footprint.getX(),
                this.footprint.getY(),
                this.footprint.getWidth(),
                this.footprint.getLength()
        );
        return StandardIsland.fromDocument(
                this.id,
                this.name,
                mappedFootprint,
                fromStoredInventory(this.inventory),
                fromStoredTradePrices(this.tradePrices)
        );
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FootprintEmbedded getFootprint() {
        return this.footprint;
    }

    public void setFootprint(FootprintEmbedded footprint) {
        this.footprint = footprint;
    }

    public Map<String, Integer> getInventory() {
        return this.inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }

    public Map<String, Integer> getTradePrices() {
        return this.tradePrices;
    }

    public void setTradePrices(Map<String, Integer> tradePrices) {
        this.tradePrices = tradePrices;
    }

    /**
     * Embedded document for footprint coordinates and dimensions.
     */
    public static class FootprintEmbedded {
        private int x;
        private int y;
        private int width;
        private int length;

        // Default constructor for Spring Data MongoDB
        public FootprintEmbedded() {
        }

        public FootprintEmbedded(int x, int y, int width, int length) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.length = length;
        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return this.width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getLength() {
            return this.length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    private static Map<String, Integer> toStoredInventory(Inventory inventory) {
        Map<String, Integer> stored = new HashMap<>();
        Inventory safeInventory = inventory == null ? Inventory.empty() : inventory;
        for (GoodType goodType : GoodType.values()) {
            stored.put(goodType.name(), safeInventory.getAmount(goodType));
        }
        return stored;
    }

    private static Inventory fromStoredInventory(Map<String, Integer> inventory) {
        if (inventory == null) {
            return Inventory.empty();
        }

        EnumMap<GoodType, Integer> amounts = new EnumMap<>(GoodType.class);
        for (GoodType goodType : GoodType.values()) {
            Integer amount = inventory.get(goodType.name());
            amounts.put(goodType, amount == null ? 0 : amount);
        }
        return Inventory.of(amounts);
    }

    private static Map<String, Integer> toStoredTradePrices(TradePriceList tradePriceList) {
        Map<String, Integer> stored = new HashMap<>();
        TradePriceList safeTradePriceList = tradePriceList == null
                ? TradePriceList.defaultPrices()
                : tradePriceList;

        for (GoodType goodType : GoodType.tradeableGoods()) {
            stored.put(goodType.name(), safeTradePriceList.getPrice(goodType));
        }
        return stored;
    }

    private static TradePriceList fromStoredTradePrices(Map<String, Integer> tradePrices) {
        if (tradePrices == null) {
            return TradePriceList.defaultPrices();
        }

        EnumMap<GoodType, Integer> mapped = new EnumMap<>(GoodType.class);
        for (GoodType goodType : GoodType.tradeableGoods()) {
            Integer value = tradePrices.get(goodType.name());
            mapped.put(goodType, value == null ? 1 : value);
        }
        return TradePriceList.of(mapped);
    }
}
