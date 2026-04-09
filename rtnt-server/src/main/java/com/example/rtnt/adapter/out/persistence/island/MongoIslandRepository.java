package com.example.rtnt.adapter.out.persistence.island;

import com.example.rtnt.domain.island.Island;
import com.example.rtnt.domain.island.IslandRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB implementation of the IslandRepository interface.
 * This bridges the domain repository interface with MongoDB persistence.
 */
@Repository
public class MongoIslandRepository implements IslandRepository {

    private final IslandMongoRepository mongoRepository;

    public MongoIslandRepository(IslandMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Island save(Island island) {
        IslandDocument document = IslandDocument.fromDomain(island);
        IslandDocument saved = this.mongoRepository.save(document);
        return saved.toDomain();
    }

    @Override
    public Optional<Island> findById(String id) {
        return this.mongoRepository.findById(id)
                .map(IslandDocument::toDomain);
    }

    @Override
    public List<Island> findAll() {
        return this.mongoRepository.findAll().stream()
                .map(IslandDocument::toDomain)
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
