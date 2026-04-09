package com.example.rtnt.adapter.out.persistence.ship;

import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.StandardShip;
import com.example.rtnt.domain.inventory.Inventory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoShipRepositoryTest {

    @Mock
    private ShipMongoRepository mongoRepository;

    @InjectMocks
    private MongoShipRepository shipRepository;

    private Ship testShip;
    private ShipDocument testDocument;

    @BeforeEach
    void setUp() {
        String id = UUID.randomUUID().toString();
        testShip = StandardShip.fromDocument(id, "Black Pearl", "island-1", null, "player-1", Inventory.empty());
        testDocument = new ShipDocument(id, "Black Pearl", "island-1", null, "player-1", null);
    }

    @Test
    void testSave() {
        // Given
        ShipDocument savedDocument = new ShipDocument(testShip.getId(), testShip.getName(), testShip.getIslandId(), null, testShip.getPlayerId(), null);
        when(mongoRepository.save(any(ShipDocument.class))).thenReturn(savedDocument);

        // When
        Ship saved = shipRepository.save(testShip);

        // Then
        assertNotNull(saved);
        assertEquals(testShip.getId(), saved.getId());
        assertEquals(testShip.getName(), saved.getName());
        assertEquals(testShip.getIslandId(), saved.getIslandId());
        assertEquals(testShip.getPlayerId(), saved.getPlayerId());
        verify(mongoRepository, times(1)).save(any(ShipDocument.class));
    }

    @Test
    void testFindById() {
        // Given
        when(mongoRepository.findById(testShip.getId())).thenReturn(Optional.of(testDocument));

        // When
        Optional<Ship> found = shipRepository.findById(testShip.getId());

        // Then
        assertTrue(found.isPresent());
        Ship foundShip = found.get();
        assertEquals(testShip.getId(), foundShip.getId());
        assertEquals(testShip.getName(), foundShip.getName());
        assertEquals(testShip.getIslandId(), foundShip.getIslandId());
        assertEquals(testShip.getPlayerId(), foundShip.getPlayerId());
        verify(mongoRepository, times(1)).findById(testShip.getId());
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        String nonExistentId = UUID.randomUUID().toString();
        when(mongoRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Ship> found = shipRepository.findById(nonExistentId);

        // Then
        assertFalse(found.isPresent());
        verify(mongoRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testFindAll() {
        // Given
        ShipDocument document1 = new ShipDocument(UUID.randomUUID().toString(), "Black Pearl", "island-a", null, "player-a", null);
        ShipDocument document2 = new ShipDocument(UUID.randomUUID().toString(), "Interceptor", "island-b", null, "player-b", null);
        List<ShipDocument> documents = Arrays.asList(document1, document2);
        when(mongoRepository.findAll()).thenReturn(documents);

        // When
        List<Ship> all = shipRepository.findAll();

        // Then
        assertNotNull(all);
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(s -> s.getName().equals("Black Pearl")));
        assertTrue(all.stream().anyMatch(s -> s.getName().equals("Interceptor")));
        assertTrue(all.stream().anyMatch(s -> "island-a".equals(s.getIslandId())));
        assertTrue(all.stream().anyMatch(s -> "island-b".equals(s.getIslandId())));
        assertTrue(all.stream().anyMatch(s -> "player-a".equals(s.getPlayerId())));
        assertTrue(all.stream().anyMatch(s -> "player-b".equals(s.getPlayerId())));
        verify(mongoRepository, times(1)).findAll();
    }

    @Test
    void testFindAllEmpty() {
        // Given
        when(mongoRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Ship> all = shipRepository.findAll();

        // Then
        assertNotNull(all);
        assertTrue(all.isEmpty());
        verify(mongoRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        // Given
        String id = testShip.getId();
        doNothing().when(mongoRepository).deleteById(id);

        // When
        shipRepository.deleteById(id);

        // Then
        verify(mongoRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteAll() {
        // Given
        doNothing().when(mongoRepository).deleteAll();

        // When
        shipRepository.deleteAll();

        // Then
        verify(mongoRepository, times(1)).deleteAll();
    }

    @Test
    void testExistsById() {
        // Given
        String id = testShip.getId();
        when(mongoRepository.existsById(id)).thenReturn(true);

        // When
        boolean exists = shipRepository.existsById(id);

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
        boolean exists = shipRepository.existsById(id);

        // Then
        assertFalse(exists);
        verify(mongoRepository, times(1)).existsById(id);
    }

    @Test
    void testSaveConvertsDomainToDocument() {
        // Given
        ShipDocument savedDocument = new ShipDocument(testShip.getId(), testShip.getName(), testShip.getIslandId(), null, testShip.getPlayerId(), null);
        when(mongoRepository.save(any(ShipDocument.class))).thenReturn(savedDocument);

        // When
        shipRepository.save(testShip);

        // Then
        verify(mongoRepository).save(argThat(document ->
                document.getId().equals(testShip.getId()) &&
                document.getName().equals(testShip.getName()) &&
                document.getIslandId().equals(testShip.getIslandId()) &&
                document.getPlayerId().equals(testShip.getPlayerId())
        ));
    }

    @Test
    void testFindByIdConvertsDocumentToDomain() {
        // Given
        when(mongoRepository.findById(testShip.getId())).thenReturn(Optional.of(testDocument));

        // When
        Optional<Ship> result = shipRepository.findById(testShip.getId());

        // Then
        assertTrue(result.isPresent());
        Ship ship = result.get();
        assertEquals(testDocument.getId(), ship.getId());
        assertEquals(testDocument.getName(), ship.getName());
        assertEquals(testDocument.getIslandId(), ship.getIslandId());
        assertEquals(testDocument.getPlayerId(), ship.getPlayerId());
    }

    @Test
    void testFindByIslandId() {
        // Given
        String islandId = "island-z";
        ShipDocument document = new ShipDocument(UUID.randomUUID().toString(), "Tempest", islandId, null, "player-z", null);
        when(mongoRepository.findByIslandId(islandId)).thenReturn(List.of(document));

        // When
        List<Ship> ships = shipRepository.findByIslandId(islandId);

        // Then
        assertEquals(1, ships.size());
        assertEquals("Tempest", ships.get(0).getName());
        assertEquals(islandId, ships.get(0).getIslandId());
        assertEquals("player-z", ships.get(0).getPlayerId());
        verify(mongoRepository, times(1)).findByIslandId(islandId);
    }

    @Test
    void testFindByPlayerId() {
        String playerId = "player-42";
        ShipDocument document = new ShipDocument(UUID.randomUUID().toString(), "Stormrider", "island-x", null, playerId, null);
        when(mongoRepository.findByPlayerId(playerId)).thenReturn(List.of(document));

        List<Ship> ships = shipRepository.findByPlayerId(playerId);

        assertEquals(1, ships.size());
        assertEquals("Stormrider", ships.get(0).getName());
        assertEquals(playerId, ships.get(0).getPlayerId());
        verify(mongoRepository, times(1)).findByPlayerId(playerId);
    }

}
