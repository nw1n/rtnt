package com.example.rtnt.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IslandNames {
    private static final List<String> NAMES = List.of(
            "Jamaica", "Cuba", "Hispaniola", "Puerto Rico", "Trinidad", "Tobago",
            "Barbados", "Martinique", "Guadeloupe", "Saint Lucia", "Grenada", "Dominica",
            "Antigua", "Aruba", "Curaçao", "Grand Cayman", "Tortola", "Saint Thomas"
    );

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public String next() {
        int index = Math.floorMod(this.currentIndex.getAndIncrement(), NAMES.size());
        return NAMES.get(index);
    }

    public void reset() {
        this.currentIndex.set(0);
    }
}
