package com.example.rtnt.game.ship.service;

import com.example.rtnt.game.ship.domain.ShipNames;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class ShipNamesLoader {
    private static final String RESOURCE = "data/ship-names.yml";

    private ShipNamesLoader() {
    }

    public static ShipNames load() {
        try (InputStream inputStream = new ClassPathResource(RESOURCE).getInputStream()) {
            return new ShipNames(shipNamesFrom(new Yaml().load(inputStream)));
        } catch (IOException | YAMLException e) {
            throw new IllegalStateException("Failed to load ship names from " + RESOURCE, e);
        }
    }

    private static List<String> shipNamesFrom(Object yaml) {
        Map<?, ?> data = (Map<?, ?>) yaml;
        List<?> ships = (List<?>) data.get("ships");
        return ships.stream().map(Object::toString).toList();
    }
}
