package com.example.rtnt.adapter.out.reference;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring-managed source of Caribbean ship names loaded from YAML data files.
 */
@Component
public class CaribbeanShipNames {

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<String> shipNames = loadShipNames();

    /**
     * Load ship names from YAML file
     * @return list of ship names
     */
    @SuppressWarnings("unchecked")
    private List<String> loadShipNames() {
        try {
            InputStream inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/ship-names.yml");

            if (inputStream == null) {
                throw new RuntimeException("Could not find data/ship-names.yml in classpath");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            if (data == null || !data.containsKey("ships")) {
                throw new RuntimeException("YAML file does not contain 'ships' key");
            }

            Object shipsObj = data.get("ships");
            if (shipsObj instanceof List) {
                List<String> ships = new ArrayList<>();
                for (Object item : (List<?>) shipsObj) {
                    if (item instanceof String) {
                        ships.add((String) item);
                    }
                }
                return ships;
            } else {
                throw new RuntimeException("'ships' key does not contain a list");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load ship names from YAML", e);
        }
    }

    public String getNext() {
        if (this.shipNames.isEmpty()) {
            throw new IllegalStateException("No ship names loaded");
        }
        int index = Math.floorMod(this.currentIndex.getAndIncrement(), this.shipNames.size());
        return this.shipNames.get(index);
    }

    public int getCurrentIndex() {
        return this.currentIndex.get();
    }

    public void reset() {
        this.currentIndex.set(0);
    }

    public String getRandom() {
        if (this.shipNames.isEmpty()) {
            throw new IllegalStateException("No ship names loaded");
        }
        int index = ThreadLocalRandom.current().nextInt(this.shipNames.size());
        return this.shipNames.get(index);
    }

    public List<String> getAll() {
        return new ArrayList<>(this.shipNames);
    }
}
