package com.example.rtnt.game.island.service;

import com.example.rtnt.game.island.domain.IslandNames;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class IslandNamesLoader {
    private static final String RESOURCE = "data/caribbean-islands.yml";

    private IslandNamesLoader() {
    }

    public static IslandNames load() {
        try (InputStream inputStream = new ClassPathResource(RESOURCE).getInputStream()) {
            return new IslandNames(islandNamesFrom(new Yaml().load(inputStream)));
        } catch (IOException | YAMLException e) {
            throw new IllegalStateException("Failed to load island names from " + RESOURCE, e);
        }
    }

    private static List<String> islandNamesFrom(Object yaml) {
        Map<?, ?> data = (Map<?, ?>) yaml;
        List<?> islands = (List<?>) data.get("islands");
        return islands.stream().map(Object::toString).toList();
    }
}
