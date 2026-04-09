package com.example.rtnt.adapter.out.persistence.journey;

import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.JourneyRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MongoJourneyRepository implements JourneyRepository {

    private static final int TRIM_BATCH_SIZE = 10_000;

    private final JourneyMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    public MongoJourneyRepository(JourneyMongoRepository mongoRepository, MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Journey save(Journey journey) {
        JourneyDocument saved = this.mongoRepository.save(JourneyDocument.fromDomain(journey));
        return saved.toDomain();
    }

    @Override
    public List<Journey> findLatest100() {
        return this.mongoRepository.findTop100ByOrderByDepartedDesc().stream()
                .map(JourneyDocument::toDomain)
                .toList();
    }

    @Override
    public List<Journey> findByShipId(String shipId) {
        return this.mongoRepository.findByShipId(shipId).stream()
                .map(JourneyDocument::toDomain)
                .toList();
    }

    @Override
    public void deleteAll() {
        this.mongoRepository.deleteAll();
    }

    @Override
    public int trimToMaxSize(int maxSize) {
        int totalRemoved = 0;
        while (true) {
            long count = this.mongoRepository.count();
            if (count <= maxSize) {
                break;
            }
            long excess = count - maxSize;
            int batch = (int) Math.min(excess, TRIM_BATCH_SIZE);
            Query query = new Query()
                    .with(Sort.by(Sort.Direction.ASC, "departed"))
                    .limit(batch);
            List<JourneyDocument> oldest = this.mongoTemplate.find(query, JourneyDocument.class);
            if (oldest.isEmpty()) {
                break;
            }
            List<String> ids = oldest.stream().map(JourneyDocument::getId).toList();
            this.mongoTemplate.remove(Query.query(Criteria.where("id").in(ids)), JourneyDocument.class);
            totalRemoved += oldest.size();
        }
        return totalRemoved;
    }
}
