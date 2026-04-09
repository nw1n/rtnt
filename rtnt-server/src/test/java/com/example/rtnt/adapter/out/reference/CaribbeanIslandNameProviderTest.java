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
class CaribbeanIslandNameProviderTest {

    @Mock
    private CaribbeanIslandNames caribbeanIslandNames;

    @InjectMocks
    private CaribbeanIslandNameProvider provider;

    @Test
    void getNextNameDelegatesToCaribbeanIslandNames() {
        // Given
        when(caribbeanIslandNames.getNext()).thenReturn("Barbados");

        // When
        String result = provider.getNextName();

        // Then
        assertEquals("Barbados", result);
        verify(caribbeanIslandNames, times(1)).getNext();
    }

    @Test
    void getRandomNameDelegatesToCaribbeanIslandNames() {
        // Given
        when(caribbeanIslandNames.getRandom()).thenReturn("Dominica");

        // When
        String result = provider.getRandomName();

        // Then
        assertEquals("Dominica", result);
        verify(caribbeanIslandNames, times(1)).getRandom();
    }
}
