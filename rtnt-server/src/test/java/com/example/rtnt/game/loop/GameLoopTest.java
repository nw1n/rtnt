package com.example.rtnt.game.loop;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.core.flow.GameFlowStatus;
import com.example.rtnt.game.core.flow.FlowMode;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusDocument;
import com.example.rtnt.game.core.flow.persistence.GameFlowStatusMongoRepository;
import com.example.rtnt.game.core.loop.GameCommand;
import com.example.rtnt.game.core.loop.GameCommandQueue;
import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.weather.TemperatureChanged;
import com.example.rtnt.game.weather.WeatherChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLoopTest {

    @Mock
    private GameFlowStatusMongoRepository gameFlowStatusMongoRepository;

    @Mock
    private EventStore eventStore;

    @Mock
    private WeatherChange weatherChange;

    private GameCommandQueue gameCommandQueue;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        this.gameCommandQueue = new GameCommandQueue();
        lenient().when(this.eventStore.readAll()).thenReturn(List.of());
        lenient().when(this.weatherChange.decide(org.mockito.ArgumentMatchers.anyLong())).thenReturn(Optional.empty());
        this.gameLoop = new GameLoop(
                this.gameFlowStatusMongoRepository,
                this.gameCommandQueue,
                this.eventStore,
                this.weatherChange
        );
    }

    @Test
    void startsInBatchPausedAtTickZero() {
        when(this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        GameFlowStatus status = this.gameLoop.get();

        assertEquals(0, status.tick());
        assertEquals(FlowMode.BATCH, status.mode());
        assertTrue(status.paused());
        assertEquals(20, this.gameLoop.weather().temperature());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(FlowMode.BATCH, saved.mode());
        assertTrue(saved.paused());
    }

    @Test
    void restoresClockAndWeatherFromEvents() {
        when(this.eventStore.readAll()).thenReturn(List.of(
                new TemperatureChanged(10, 2),
                new TemperatureChanged(40, -1)
        ));
        this.givenFlow(FlowMode.BATCH, true);

        assertEquals(40, this.gameLoop.get().tick());
        assertEquals(21, this.gameLoop.weather().temperature());
    }

    @Test
    void quietTickDoesNotAppend() {
        this.givenFlow(FlowMode.BATCH, false);

        this.gameLoop.step();

        assertEquals(1, this.gameLoop.get().tick());
        verify(this.eventStore, never()).append(org.mockito.ArgumentMatchers.any());
        assertEquals(20, this.gameLoop.weather().temperature());
    }

    @Test
    void temperatureChangeAppendsAndApplies() {
        this.givenFlow(FlowMode.BATCH, false);
        when(this.weatherChange.decide(1)).thenReturn(Optional.of(new TemperatureChanged(1, 3)));

        this.gameLoop.step();

        verify(this.eventStore).append(new TemperatureChanged(1, 3));
        assertEquals(23, this.gameLoop.weather().temperature());
    }

    @Test
    void stepAdvancesTickAndDrainsCommandsForThatTick() {
        this.givenFlow(FlowMode.BATCH, false);
        this.gameCommandQueue.enqueue(0, new GameCommand("depart"));
        this.gameCommandQueue.enqueue(1, new GameCommand("later"));

        GameFlowStatus status = this.gameLoop.step();

        assertEquals(1, status.tick());
        assertTrue(this.gameCommandQueue.drain(0).isEmpty());
        assertEquals(1, this.gameCommandQueue.drain(1).size());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void stepIfLiveDoesNothingWhenPaused() {
        this.givenFlow(FlowMode.LIVE, true);

        this.gameLoop.stepIfLive();

        assertEquals(0, this.gameLoop.get().tick());
        verify(this.weatherChange, never()).decide(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void advanceRejectsNonPositiveTicks() {
        assertThrows(IllegalArgumentException.class, () -> this.gameLoop.advance(0));
    }

    @Test
    void pauseUpdatesFlagWithoutTicking() {
        when(this.eventStore.readAll()).thenReturn(List.of(new TemperatureChanged(12, 1)));
        this.givenFlow(FlowMode.LIVE, false);

        GameFlowStatus paused = this.gameLoop.pause();

        assertTrue(paused.paused());
        assertEquals(12, paused.tick());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertTrue(saved.paused());
        assertEquals(FlowMode.LIVE, saved.mode());
    }

    @Test
    void setModeBatchDoesNotPersist() {
        this.givenFlow(FlowMode.LIVE, false);

        GameFlowStatus status = this.gameLoop.setMode(FlowMode.BATCH);

        assertEquals(FlowMode.BATCH, status.mode());
        verify(this.gameFlowStatusMongoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void setModeLiveUnpausesAndPersists() {
        this.givenFlow(FlowMode.BATCH, true);

        GameFlowStatus status = this.gameLoop.setMode(FlowMode.LIVE);

        assertEquals(FlowMode.LIVE, status.mode());
        assertFalse(status.paused());
        GameFlowStatusDocument saved = this.capturedFlow();
        assertEquals(FlowMode.LIVE, saved.mode());
        assertFalse(saved.paused());
    }

    @Test
    void readsMongoOnceThenUsesMemory() {
        this.givenFlow(FlowMode.LIVE, false);

        this.gameLoop.get();
        this.gameLoop.step();
        this.gameLoop.stepIfLive();

        verify(this.eventStore, times(1)).readAll();
        verify(this.gameFlowStatusMongoRepository, times(1)).findById(GameFlowStatusDocument.DOCUMENT_ID);
        assertEquals(2, this.gameLoop.get().tick());
    }

    private void givenFlow(FlowMode mode, boolean paused) {
        when(this.gameFlowStatusMongoRepository.findById(GameFlowStatusDocument.DOCUMENT_ID))
                .thenReturn(Optional.of(new GameFlowStatusDocument(
                        GameFlowStatusDocument.DOCUMENT_ID,
                        mode,
                        paused
                )));
    }

    private GameFlowStatusDocument capturedFlow() {
        ArgumentCaptor<GameFlowStatusDocument> captor = ArgumentCaptor.forClass(GameFlowStatusDocument.class);
        verify(this.gameFlowStatusMongoRepository).save(captor.capture());
        return captor.getValue();
    }
}
