package com.example.rtnt.domain.player;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public class StandardPlayer implements Player {
    private final String id;
    private final String name;
    private final String hexColor;

    private StandardPlayer(String id, String name, String hexColor) {
        this.id = Objects.requireNonNull(id, "Player id cannot be null");
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        this.hexColor = Objects.requireNonNull(hexColor, "Player hex color cannot be null");
    }

    public static StandardPlayer create(String name) {
        return new StandardPlayer(UUID.randomUUID().toString(), name, randomHexColor());
    }

    public static StandardPlayer fromDocument(String id, String name, String hexColor) {
        return new StandardPlayer(id, name, hexColor);
    }

    private static String randomHexColor() {
        int colorValue = ThreadLocalRandom.current().nextInt(0x1000000);
        return String.format("#%06X", colorValue);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getHexColor() {
        return this.hexColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        StandardPlayer that = (StandardPlayer) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "StandardPlayer{id='" + this.id + "', name='" + this.name + "', hexColor='" + this.hexColor + "'}";
    }
}
