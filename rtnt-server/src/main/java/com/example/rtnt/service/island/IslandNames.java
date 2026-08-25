package com.example.rtnt.service.island;

import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IslandNames {
    private static final String RESOURCE = "data/caribbean-islands.yml";

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<String> names = initNames();

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public String next() {
        int i = this.currentIndex.getAndIncrement();
        var isFreeNameLeft = i < this.names.size();

        if (isFreeNameLeft) {
            return this.names.get(i);
        } else {
            return "Island " + (i + 1);
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/

    private static List<String> initNames() {
        try (InputStream inputStream = new ClassPathResource(RESOURCE).getInputStream()) {
            return islandNamesFrom(new Yaml().load(inputStream));
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
