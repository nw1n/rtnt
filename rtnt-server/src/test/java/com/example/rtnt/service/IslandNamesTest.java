package com.example.rtnt.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandNamesTest {

    @Test
    void allLoadsNamesFromYaml() {
        IslandNames names = new IslandNames();
        List<String> all = names.all();

        assertFalse(all.isEmpty());
        assertTrue(all.contains("Jamaica"));
        assertTrue(all.contains("Cuba"));
        assertTrue(all.contains("Puerto Rico"));
    }

    @Test
    void nextFollowsYamlOrderAfterReset() {
        IslandNames names = new IslandNames();

        names.reset();
        assertEquals("Jamaica", names.next());
        assertEquals("Cuba", names.next());
        assertEquals("Hispaniola", names.next());
    }

    @Test
    void allReturnsUnmodifiableCopy() {
        IslandNames names = new IslandNames();

        assertThrows(UnsupportedOperationException.class, () -> names.all().clear());
        assertFalse(names.all().isEmpty());
    }
}
