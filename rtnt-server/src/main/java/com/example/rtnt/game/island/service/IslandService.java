package com.example.rtnt.game.island.service;

import com.example.rtnt.game.core.event.EventStore;
import com.example.rtnt.game.core.event.IslandCreated;
import com.example.rtnt.game.core.event.IslandPopulationGrew;
import com.example.rtnt.game.core.event.World;
import com.example.rtnt.game.core.event.WorldCleared;
import com.example.rtnt.game.core.event.WorldEvent;
import com.example.rtnt.game.core.ticker.persistence.TickerDocument;
import com.example.rtnt.game.core.ticker.persistence.TickerMongoRepository;
import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandPlacement;
import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.island.persistence.IslandDocument;
import com.example.rtnt.game.island.persistence.IslandMongoRepository;
import com.example.rtnt.game.island.persistence.IslandStatusDocument;
import com.example.rtnt.game.island.persistence.IslandStatusMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IslandService {
    private static final Logger log = LoggerFactory.getLogger(IslandService.class);

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final IslandMongoRepository islandMongoRepository;
    private final IslandStatusMongoRepository islandStatusMongoRepository;
    private final EventStore eventStore;
    private final TickerMongoRepository tickerMongoRepository;
    private final int islandCount;
    private final int mapWidth;
    private final int mapHeight;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public IslandService(
            IslandMongoRepository islandMongoRepository,
            IslandStatusMongoRepository islandStatusMongoRepository,
            EventStore eventStore,
            TickerMongoRepository tickerMongoRepository,
            @Value("${rtnt.startup.island-count:15}") int islandCount,
            @Value("${rtnt.map.width:2000}") int mapWidth,
            @Value("${rtnt.map.height:1000}") int mapHeight
    ) {
        this.islandMongoRepository = islandMongoRepository;
        this.islandStatusMongoRepository = islandStatusMongoRepository;
        this.eventStore = eventStore;
        this.tickerMongoRepository = tickerMongoRepository;
        this.islandCount = islandCount;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public List<Island> list() {
        return this.islandMongoRepository.findAll().stream()
                .map(IslandDocument::toIsland)
                .toList();
    }

    public List<IslandStatus> listStatuses() {
        return this.islandStatusMongoRepository.findAll().stream()
                .map(IslandStatusDocument::toIslandStatus)
                .toList();
    }

    public List<Island> recreateAll() {
        long tick = this.currentTick();
        List<WorldEvent> events = new ArrayList<>();
        events.add(new WorldCleared(tick));
        events.addAll(this.decideSeed(tick));
        this.eventStore.append(events);
        World world = World.empty().applyAll(events);
        this.project(world);
        log.info("Recreated {} islands", world.islands().size());
        return world.islands();
    }

    public void project(World world) {
        this.replaceAll(world.islands(), world.islandStatuses());
    }

    public void replaceAll(List<Island> islands, List<IslandStatus> statuses) {
        this.islandStatusMongoRepository.deleteAll();
        this.islandMongoRepository.deleteAll();
        this.islandMongoRepository.saveAll(islands.stream().map(IslandDocument::fromIsland).toList());
        this.islandStatusMongoRepository.saveAll(statuses.stream().map(IslandStatusDocument::from).toList());
        log.info("Replaced world with {} islands from projection", islands.size());
    }

    public void seedIfEmpty() {
        if (!this.eventStore.isEmpty()) {
            if (this.islandMongoRepository.count() == 0) {
                List<WorldEvent> events = this.eventStore.readAll();
                this.project(World.empty().applyAll(events));
                log.info("Rebuilt island projection from {} events", events.size());
            }
            return;
        }
        if (this.islandMongoRepository.count() > 0) {
            this.backfillEventsFromProjection();
            return;
        }
        List<WorldEvent> created = this.decideSeed(this.currentTick());
        this.eventStore.append(created);
        this.project(World.empty().applyAll(created));
        log.info("Seeded {} islands", created.size());
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private List<WorldEvent> decideSeed(long tick) {
        var islandNames = IslandNamesLoader.load();
        var islandPlacement = new IslandPlacement(this.mapWidth, this.mapHeight);
        List<Island> islands = new ArrayList<>();
        List<WorldEvent> events = new ArrayList<>();
        for (int i = 0; i < this.islandCount; i++) {
            var newIsland = islandPlacement.placeIsland(islandNames.next(), islands);
            islands.add(newIsland);
            events.add(IslandCreated.from(newIsland, tick));
        }
        return events;
    }

    private void backfillEventsFromProjection() {
        Map<String, Long> populationByIslandId = this.listStatuses().stream()
                .collect(Collectors.toMap(IslandStatus::islandId, IslandStatus::population, (left, right) -> left));
        long tick = this.currentTick();
        List<WorldEvent> events = new ArrayList<>();
        for (Island island : this.list()) {
            events.add(IslandCreated.from(island, tick));
            long population = populationByIslandId.getOrDefault(island.id(), 0L);
            if (population > 0) {
                events.add(new IslandPopulationGrew(tick, island.id(), population));
            }
        }
        this.eventStore.append(events);
        log.info("Backfilled {} events from existing island projection", events.size());
    }

    private long currentTick() {
        return this.tickerMongoRepository.findById(TickerDocument.DOCUMENT_ID)
                .map(TickerDocument::tick)
                .orElse(0L);
    }
}
