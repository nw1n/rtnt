package com.example.rtnt.adapter.out.persistence.journey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoJourneyRepositoryTest {

    @Mock
    private JourneyMongoRepository mongoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private MongoJourneyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MongoJourneyRepository(mongoRepository, mongoTemplate);
    }

    @Test
    void trimToMaxSizeDoesNothingWhenUnderCap() {
        when(mongoRepository.count()).thenReturn(100L);

        int removed = repository.trimToMaxSize(3000);

        assertEquals(0, removed);
        verify(mongoTemplate, never()).find(any(Query.class), eq(JourneyDocument.class));
    }

    @Test
    void trimToMaxSizeRemovesOldestUntilCap() {
        when(mongoRepository.count()).thenReturn(4002L, 3000L);
        JourneyDocument old1 = doc("a", Instant.parse("2020-01-01T00:00:00Z"));
        JourneyDocument old2 = doc("b", Instant.parse("2020-01-02T00:00:00Z"));
        when(mongoTemplate.find(any(Query.class), eq(JourneyDocument.class)))
                .thenReturn(List.of(old1, old2));

        int removed = repository.trimToMaxSize(3000);

        assertEquals(2, removed);
        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(JourneyDocument.class));
    }

    private static JourneyDocument doc(String id, Instant departed) {
        JourneyDocument d = new JourneyDocument();
        d.setId(id);
        d.setDeparted(departed);
        return d;
    }
}
