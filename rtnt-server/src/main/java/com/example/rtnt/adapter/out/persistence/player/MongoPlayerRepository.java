package com.example.rtnt.adapter.out.persistence.player;

import com.example.rtnt.domain.player.Player;
import com.example.rtnt.domain.player.PlayerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MongoPlayerRepository implements PlayerRepository {

    private final PlayerMongoRepository mongoRepository;

    public MongoPlayerRepository(PlayerMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Player save(Player player) {
        PlayerDocument saved = this.mongoRepository.save(PlayerDocument.fromDomain(player));
        return saved.toDomain();
    }

    @Override
    public Optional<Player> findById(String id) {
        return this.mongoRepository.findById(id).map(PlayerDocument::toDomain);
    }

    @Override
    public List<Player> findAll() {
        return this.mongoRepository.findAll().stream().map(PlayerDocument::toDomain).toList();
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
    public boolean existsById(String id) {
        return this.mongoRepository.existsById(id);
    }
}
