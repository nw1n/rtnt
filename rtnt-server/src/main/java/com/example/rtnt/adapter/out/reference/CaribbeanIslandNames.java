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
 * Spring-managed source of Caribbean island names loaded from YAML data files.
 */
@Component
public class CaribbeanIslandNames {

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<String> islandNames = loadIslandNames();

    /**
     * Load island names from YAML file
     * @return list of Caribbean island names
     */
    @SuppressWarnings("unchecked")
    private List<String> loadIslandNames() {
        try {
            InputStream inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/caribbean-islands.yml");

            if (inputStream == null) {
                throw new RuntimeException("Could not find data/caribbean-islands.yml in classpath");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            if (data == null || !data.containsKey("islands")) {
                throw new RuntimeException("YAML file does not contain 'islands' key");
            }

            Object islandsObj = data.get("islands");
            if (islandsObj instanceof List) {
                List<String> islands = new ArrayList<>();
                for (Object item : (List<?>) islandsObj) {
                    if (item instanceof String) {
                        islands.add((String) item);
                    }
                }
                return islands;
            } else {
                throw new RuntimeException("'islands' key does not contain a list");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Caribbean island names from YAML", e);
        }
    }

    public String getNext() {
        if (this.islandNames.isEmpty()) {
            throw new IllegalStateException("No island names loaded");
        }
        int index = Math.floorMod(this.currentIndex.getAndIncrement(), this.islandNames.size());
        return this.islandNames.get(index);
    }

    public int getCurrentIndex() {
        return this.currentIndex.get();
    }

    public void reset() {
        this.currentIndex.set(0);
    }

    public String getRandom() {
        if (this.islandNames.isEmpty()) {
            throw new IllegalStateException("No island names loaded");
        }
        int index = ThreadLocalRandom.current().nextInt(this.islandNames.size());
        return this.islandNames.get(index);
    }

    public List<String> getAll() {
        return new ArrayList<>(this.islandNames);
    }
}
