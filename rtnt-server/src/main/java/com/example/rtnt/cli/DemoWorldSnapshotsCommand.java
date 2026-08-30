package com.example.rtnt.cli;

import com.example.rtnt.game.loop.GameLoop;
import com.example.rtnt.game.loop.GameLoopStatus;
import com.example.rtnt.game.loop.persistence.WorldSnapshotDocument;
import com.example.rtnt.game.loop.persistence.WorldSnapshotMongoRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class DemoWorldSnapshotsCommand implements ApplicationRunner {
    public static final String ARG = "demo-world-snapshots";
    private static final int DEFAULT_TICKS = 10_000;

    private final GameLoop gameLoop;
    private final WorldSnapshotMongoRepository worldSnapshotMongoRepository;
    private final ConfigurableApplicationContext context;
    private final int snapshotIntervalTicks;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public DemoWorldSnapshotsCommand(
            GameLoop gameLoop,
            WorldSnapshotMongoRepository worldSnapshotMongoRepository,
            ConfigurableApplicationContext context,
            @Value("${rtnt.clock.snapshot-interval-ticks:1000}") int snapshotIntervalTicks
    ) {
        this.gameLoop = gameLoop;
        this.worldSnapshotMongoRepository = worldSnapshotMongoRepository;
        this.context = context;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (!args.getNonOptionArgs().contains(ARG)) {
            return;
        }
        int ticks = this.parseTicks(args);
        long startTick = this.gameLoop.get().tick();
        long snapshotsBefore = this.worldSnapshotMongoRepository.count();

        System.out.println("Demo: advance " + ticks + " ticks, snapshot every "
                + this.snapshotIntervalTicks);
        System.out.println("Start tick: " + startTick);
        System.out.println("Snapshots in DB before: " + snapshotsBefore);

        GameLoopStatus status = this.gameLoop.advance(ticks);
        long endTick = status.tick();
        long expectedNew = this.snapshotCountInRange(startTick, endTick);

        List<Long> newTicks = this.worldSnapshotMongoRepository.findAll().stream()
                .map(WorldSnapshotDocument::tick)
                .filter(tick -> tick > startTick && tick <= endTick)
                .sorted()
                .toList();
        long snapshotsAfter = this.worldSnapshotMongoRepository.count();
        boolean tickZeroPresent = this.worldSnapshotMongoRepository.existsById(0L);
        boolean pass = newTicks.size() == expectedNew && (startTick > 0 || tickZeroPresent);

        System.out.println("End tick: " + endTick);
        System.out.println("Snapshot at tick 0 present: " + tickZeroPresent);
        System.out.println("Expected new snapshots: " + expectedNew);
        System.out.println("New snapshot ticks: " + newTicks);
        System.out.println("Snapshots in DB after: " + snapshotsAfter);
        if (!newTicks.isEmpty()) {
            int islands = this.worldSnapshotMongoRepository.findById(newTicks.getLast())
                    .map(document -> document.islands().size())
                    .orElse(0);
            System.out.println("Islands in last snapshot: " + islands);
        }
        System.out.println(pass ? "PASS" : "FAIL");

        System.exit(SpringApplication.exit(this.context, () -> pass ? 0 : 1));
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private long snapshotCountInRange(long startTickExclusive, long endTickInclusive) {
        return endTickInclusive / this.snapshotIntervalTicks
                - startTickExclusive / this.snapshotIntervalTicks;
    }

    private int parseTicks(ApplicationArguments args) {
        List<String> nonOption = args.getNonOptionArgs();
        int index = nonOption.indexOf(ARG);
        if (index + 1 >= nonOption.size()) {
            return DEFAULT_TICKS;
        }
        return Integer.parseInt(nonOption.get(index + 1));
    }
}
