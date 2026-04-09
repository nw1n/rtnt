package com.example.rtnt.usecase.ship;

import com.example.rtnt.domain.ship.JourneyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JourneyRetentionSchedulerTest {

    @Mock
    private JourneyRepository journeyRepository;

    private JourneyRetentionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new JourneyRetentionScheduler(journeyRepository, 3000);
    }

    @Test
    void trimExcessJourneysDelegatesToRepository() {
        when(journeyRepository.trimToMaxSize(3000)).thenReturn(5);

        scheduler.trimExcessJourneys();

        verify(journeyRepository, times(1)).trimToMaxSize(3000);
    }
}
