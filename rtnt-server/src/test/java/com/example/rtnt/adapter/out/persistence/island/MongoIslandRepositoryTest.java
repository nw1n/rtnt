package com.example.rtnt.adapter.out.persistence.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.StandardIsland;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.island.TradePriceList;
import com.example.rtnt.domain.location.Footprint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoIslandRepositoryTest {

    @Mock
    private IslandMongoRepository mongoRepository;

    @InjectMocks
    private MongoIslandRepository islandRepository;

    private Island testIsland;
    private IslandDocument testDocument;

    @BeforeEach
    void setUp() {
        String id = UUID.randomUUID().toString();
        testIsland = StandardIsland.fromDocument(
                id,
                "Jamaica",
                Footprint.create(10, 20, 50, 50),
                Inventory.empty(),
                TradePriceList.defaultPrices()
        );
        IslandDocument.FootprintEmbedded footprintEmbedded = new IslandDocument.FootprintEmbedded(10, 20, 50, 50);
        testDocument = new IslandDocument(id, "Jamaica", footprintEmbedded);
    }

    @Test
    void testSave() {
        // Given
        IslandDocument savedDocument = new IslandDocument(
                testIsland.getId(),
                testIsland.getName(),
                new IslandDocument.FootprintEmbedded(10, 20, 50, 50)
        );
        when(mongoRepository.save(any(IslandDocument.class))).thenReturn(savedDocument);

        // When
        Island saved = islandRepository.save(testIsland);

        // Then
        assertNotNull(saved);
        assertEquals(testIsland.getId(), saved.getId());
        assertEquals(testIsland.getName(), saved.getName());
        assertEquals(10, saved.getFootprint().getX());
        assertEquals(20, saved.getFootprint().getY());
        verify(mongoRepository, times(1)).save(any(IslandDocument.class));
    }

    @Test
    void testFindById() {
        // Given
        when(mongoRepository.findById(testIsland.getId())).thenReturn(Optional.of(testDocument));

        // When
        Optional<Island> found = islandRepository.findById(testIsland.getId());

        // Then
        assertTrue(found.isPresent());
        Island foundIsland = found.get();
        assertEquals(testIsland.getId(), foundIsland.getId());
        assertEquals(testIsland.getName(), foundIsland.getName());
        assertEquals(10, foundIsland.getFootprint().getX());
        assertEquals(20, foundIsland.getFootprint().getY());
        verify(mongoRepository, times(1)).findById(testIsland.getId());
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        String nonExistentId = UUID.randomUUID().toString();
        when(mongoRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Island> found = islandRepository.findById(nonExistentId);

        // Then
        assertFalse(found.isPresent());
        verify(mongoRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testFindAll() {
        // Given
        IslandDocument document1 = new IslandDocument(
                UUID.randomUUID().toString(),
                "Jamaica",
                new IslandDocument.FootprintEmbedded(10, 20, 50, 50)
        );
        IslandDocument document2 = new IslandDocument(
                UUID.randomUUID().toString(),
                "Cuba",
                new IslandDocument.FootprintEmbedded(15, 25, 50, 50)
        );
        List<IslandDocument> documents = Arrays.asList(document1, document2);
        when(mongoRepository.findAll()).thenReturn(documents);

        // When
        List<Island> all = islandRepository.findAll();

        // Then
        assertNotNull(all);
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(i -> i.getName().equals("Jamaica")));
        assertTrue(all.stream().anyMatch(i -> i.getName().equals("Cuba")));
        verify(mongoRepository, times(1)).findAll();
    }

    @Test
    void testFindAllEmpty() {
        // Given
        when(mongoRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Island> all = islandRepository.findAll();

        // Then
        assertNotNull(all);
        assertTrue(all.isEmpty());
        verify(mongoRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        // Given
        String id = testIsland.getId();
        doNothing().when(mongoRepository).deleteById(id);

        // When
        islandRepository.deleteById(id);

        // Then
        verify(mongoRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteAll() {
        // Given
        doNothing().when(mongoRepository).deleteAll();

        // When
        islandRepository.deleteAll();

        // Then
        verify(mongoRepository, times(1)).deleteAll();
    }

    @Test
    void testExistsById() {
        // Given
        String id = testIsland.getId();
        when(mongoRepository.existsById(id)).thenReturn(true);

        // When
        boolean exists = islandRepository.existsById(id);

        // Then
        assertTrue(exists);
        verify(mongoRepository, times(1)).existsById(id);
    }

    @Test
    void testExistsByIdFalse() {
        // Given
        String id = UUID.randomUUID().toString();
        when(mongoRepository.existsById(id)).thenReturn(false);

        // When
        boolean exists = islandRepository.existsById(id);

        // Then
        assertFalse(exists);
        verify(mongoRepository, times(1)).existsById(id);
    }

    @Test
    void testSaveConvertsDomainToDocument() {
        // Given
        IslandDocument savedDocument = new IslandDocument(
                testIsland.getId(),
                testIsland.getName(),
                new IslandDocument.FootprintEmbedded(10, 20, 50, 50)
        );
        when(mongoRepository.save(any(IslandDocument.class))).thenReturn(savedDocument);

        // When
        islandRepository.save(testIsland);

        // Then
        verify(mongoRepository).save(argThat(document ->
                document.getId().equals(testIsland.getId()) &&
                document.getName().equals(testIsland.getName()) &&
                document.getFootprint().getX() == 10 &&
                document.getFootprint().getY() == 20
        ));
    }

    @Test
    void testFindByIdConvertsDocumentToDomain() {
        // Given
        when(mongoRepository.findById(testIsland.getId())).thenReturn(Optional.of(testDocument));

        // When
        Optional<Island> result = islandRepository.findById(testIsland.getId());

        // Then
        assertTrue(result.isPresent());
        Island island = result.get();
        assertEquals(testDocument.getId(), island.getId());
        assertEquals(testDocument.getName(), island.getName());
        assertEquals(testDocument.getFootprint().getX(), island.getFootprint().getX());
        assertEquals(testDocument.getFootprint().getY(), island.getFootprint().getY());
    }
}
