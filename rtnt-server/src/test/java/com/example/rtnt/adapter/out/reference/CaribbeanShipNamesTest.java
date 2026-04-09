package com.example.rtnt.adapter.out.reference;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaribbeanShipNamesTest {

  @Test
  void getAllLoadsNamesFromYaml() {
    // Given
    CaribbeanShipNames names = new CaribbeanShipNames();

    // When
    List<String> all = names.getAll();

    // Then
    assertFalse(all.isEmpty());
    assertTrue(all.contains("Black Pearl"));
    assertTrue(all.contains("Queen Anne's Revenge"));
    assertTrue(all.contains("Flying Dutchman"));
  }

  @Test
  void getNextFollowsYamlOrderAfterReset() {
    // Given
    CaribbeanShipNames names = new CaribbeanShipNames();

    // When
    names.reset();
    String first = names.getNext();
    String second = names.getNext();
    String third = names.getNext();

    // Then
    assertEquals("Black Pearl", first);
    assertEquals("Queen Anne's Revenge", second);
    assertEquals("Flying Dutchman", third);
  }

  @Test
  void getRandomAlwaysReturnsLoadedName() {
    // Given
    CaribbeanShipNames names = new CaribbeanShipNames();
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
    CaribbeanShipNames names = new CaribbeanShipNames();

    // When
    List<String> all = names.getAll();
    all.clear();

    // Then
    assertFalse(names.getAll().isEmpty());
  }
}
