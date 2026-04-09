package com.example.rtnt.adapter.out.reference;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaribbeanIslandNamesTest {

  @Test
  void getAllLoadsNamesFromYaml() {
    // Given
    CaribbeanIslandNames names = new CaribbeanIslandNames();

    // When
    List<String> all = names.getAll();

    // Then
    assertFalse(all.isEmpty());
    assertTrue(all.contains("Jamaica"));
    assertTrue(all.contains("Cuba"));
    assertTrue(all.contains("Puerto Rico"));
  }

  @Test
  void getNextFollowsYamlOrderAfterReset() {
    // Given
    CaribbeanIslandNames names = new CaribbeanIslandNames();

    // When
    names.reset();
    String first = names.getNext();
    String second = names.getNext();
    String third = names.getNext();

    // Then
    assertEquals("Jamaica", first);
    assertEquals("Cuba", second);
    assertEquals("Hispaniola", third);
  }

  @Test
  void getRandomAlwaysReturnsLoadedName() {
    // Given
    CaribbeanIslandNames names = new CaribbeanIslandNames();
    List<String> all = names.getAll();

    // When/Then
    for (int i = 0; i < 100; i++) {
      String randomName = names.getRandom();
      assertTrue(all.contains(randomName));
    }
  }

  @Test
  void getAllReturnsCopy() {
    // Given
    CaribbeanIslandNames names = new CaribbeanIslandNames();

    // When
    List<String> all = names.getAll();
    all.clear();

    // Then
    assertFalse(names.getAll().isEmpty());
  }
}
