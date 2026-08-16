package com.example.rtnt.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IslandNames {
    private final List<String> names;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public IslandNames() {
        this.names = load();
    }

    public String next() {
        int index = this.currentIndex.getAndUpdate(i -> (i + 1) % this.names.size());
        return this.names.get(index);
    }

    public void reset() {
        this.currentIndex.set(0);
    }

    List<String> all() {
        return List.copyOf(this.names);
    }

    private static List<String> load() {
        try (InputStream inputStream = new ClassPathResource("data/caribbean-islands.yml").getInputStream()) {
            Map<String, Object> data = new Yaml().load(inputStream);
            Object islands = data == null ? null : data.get("islands");
            if (!(islands instanceof List<?> list) || list.isEmpty()) {
                throw new IllegalStateException("data/caribbean-islands.yml must contain a non-empty 'islands' list");
            }
            return list.stream().map(Object::toString).toList();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load island names from data/caribbean-islands.yml", e);
        }
    }
}
