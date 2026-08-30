package com.example.rtnt.game.loop;

import com.example.rtnt.RtntDataTest;
import com.example.rtnt.game.core.loop.GameLoop;
import com.example.rtnt.game.core.loop.GameLoopStatus;
import com.example.rtnt.game.core.worldsnapshot.persistence.WorldSnapshotDocument;
import com.example.rtnt.game.core.worldsnapshot.persistence.WorldSnapshotMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RtntDataTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WorldSnapshotIntervalTest {
    private static final int TICKS = 10_000;

    @Autowired
    private GameLoop gameLoop;

    @Autowired
    private WorldSnapshotMongoRepository worldSnapshotMongoRepository;

    @Value("${rtnt.clock.snapshot-interval-ticks:1000}")
    private int snapshotIntervalTicks;

    @Test
    void advancingWritesIntervalSnapshotsAndKeepsTickZero() {
        assertEquals(0, this.gameLoop.get().tick());
        assertTrue(this.worldSnapshotMongoRepository.existsById(0L));

        GameLoopStatus status = this.gameLoop.advance(TICKS);
        long endTick = status.tick();
        assertEquals(TICKS, endTick);

        List<Long> snapshotTicks = this.worldSnapshotMongoRepository.findAll().stream()
                .map(WorldSnapshotDocument::tick)
                .sorted()
                .toList();
        List<Long> expectedTicks = LongStream.rangeClosed(0, endTick / this.snapshotIntervalTicks)
                .map(n -> n * this.snapshotIntervalTicks)
                .boxed()
                .toList();
        assertEquals(expectedTicks, snapshotTicks);

        WorldSnapshotDocument last = this.worldSnapshotMongoRepository.findById(endTick).orElseThrow();
        assertFalse(last.islands().isEmpty());
    }
}
