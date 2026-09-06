package com.example.rtnt.game.core.event.persistence;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.core.event.WorldEvent;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@NullMarked
public class MongoEventStore implements EventStore {
    private final EventMongoRepository eventMongoRepository;

    public MongoEventStore(EventMongoRepository eventMongoRepository) {
        this.eventMongoRepository = eventMongoRepository;
    }

    @Override
    public void append(List<WorldEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        long version = this.eventMongoRepository.findTopByStreamIdOrderByStreamVersionDesc(WORLD_STREAM)
                .map(document -> document.streamVersion() + 1)
                .orElse(1L);
        Instant recordedAt = Instant.now();
        List<EventDocument> documents = new ArrayList<>(events.size());
        for (WorldEvent event : events) {
            documents.add(WorldEventMapper.toDocument(WORLD_STREAM, version, event, recordedAt));
            version++;
        }
        this.eventMongoRepository.saveAll(documents);
    }

    @Override
    public List<WorldEvent> readAll() {
        return this.eventMongoRepository.findByStreamIdOrderByStreamVersionAsc(WORLD_STREAM).stream()
                .map(WorldEventMapper::toEvent)
                .toList();
    }

    @Override
    public List<WorldEvent> readUpToTick(long tick) {
        return this.eventMongoRepository
                .findByStreamIdAndTickLessThanEqualOrderByStreamVersionAsc(WORLD_STREAM, tick)
                .stream()
                .map(WorldEventMapper::toEvent)
                .toList();
    }

    @Override
    public void deleteAfterTick(long tick) {
        this.eventMongoRepository.deleteByStreamIdAndTickGreaterThan(WORLD_STREAM, tick);
    }

    @Override
    public boolean isEmpty() {
        return this.eventMongoRepository.countByStreamId(WORLD_STREAM) == 0;
    }
}
