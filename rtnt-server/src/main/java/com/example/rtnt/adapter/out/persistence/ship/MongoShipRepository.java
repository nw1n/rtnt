package com.example.rtnt.adapter.out.persistence.ship;

import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.domain.ship.ShipRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB implementation of the ShipRepository interface.
 * This bridges the domain repository interface with MongoDB persistence.
 */
@Repository
public class MongoShipRepository implements ShipRepository {

    private final ShipMongoRepository mongoRepository;

    public MongoShipRepository(ShipMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Ship save(Ship ship) {
        ShipDocument document = ShipDocument.fromDomain(ship);
        ShipDocument saved = this.mongoRepository.save(document);
        return saved.toDomain();
    }

    @Override
    public Optional<Ship> findById(String id) {
        return this.mongoRepository.findById(id)
                .map(ShipDocument::toDomain);
    }

    @Override
    public List<Ship> findAll() {
        return this.mongoRepository.findAll().stream()
                .map(ShipDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ship> findByIslandId(String islandId) {
        return this.mongoRepository.findByIslandId(islandId).stream()
                .map(ShipDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ship> findByPlayerId(String playerId) {
        return this.mongoRepository.findByPlayerId(playerId).stream()
                .map(ShipDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        this.mongoRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        this.mongoRepository.deleteAll();
    }

    @Override
    public long count() {
        return this.mongoRepository.count();
    }

    @Override
    public boolean existsById(String id) {
        return this.mongoRepository.existsById(id);
    }
}
