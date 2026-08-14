package com.example.rtnt.service;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IslandNames {
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<String> names = loadNames();

    @SuppressWarnings("unchecked")
    private static List<String> loadNames() {
        InputStream inputStream = IslandNames.class.getClassLoader()
                .getResourceAsStream("data/caribbean-islands.yml");
        if (inputStream == null) {
            throw new IllegalStateException("Could not find data/caribbean-islands.yml");
        }
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        if (data == null || !(data.get("islands") instanceof List<?> islandsObj)) {
            throw new IllegalStateException("YAML file does not contain an islands list");
        }
        List<String> islands = new ArrayList<>();
        for (Object item : islandsObj) {
            if (item instanceof String name) {
                islands.add(name);
            }
        }
        if (islands.isEmpty()) {
            throw new IllegalStateException("No island names loaded");
        }
        return islands;
    }

    public String next() {
        int index = Math.floorMod(this.currentIndex.getAndIncrement(), this.names.size());
        return this.names.get(index);
    }
}
