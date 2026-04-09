package com.example.rtnt.adapter.out.reference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaribbeanShipNameProviderTest {

    @Mock
    private CaribbeanShipNames caribbeanShipNames;

    @InjectMocks
    private CaribbeanShipNameProvider provider;

    @Test
    void getNextNameDelegatesToCaribbeanShipNames() {
        // Given
        when(caribbeanShipNames.getNext()).thenReturn("Black Pearl");

        // When
        String result = provider.getNextName();

        // Then
        assertEquals("Black Pearl", result);
        verify(caribbeanShipNames, times(1)).getNext();
    }

    @Test
    void getRandomNameDelegatesToCaribbeanShipNames() {
        // Given
        when(caribbeanShipNames.getRandom()).thenReturn("Flying Dutchman");

        // When
        String result = provider.getRandomName();

        // Then
        assertEquals("Flying Dutchman", result);
        verify(caribbeanShipNames, times(1)).getRandom();
    }
}
