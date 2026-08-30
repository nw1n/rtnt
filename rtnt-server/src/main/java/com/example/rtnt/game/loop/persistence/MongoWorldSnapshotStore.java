package com.example.rtnt.game.loop.persistence;

import com.example.rtnt.game.clock.domain.GameClock;
import com.example.rtnt.game.loop.WorldSnapshotStore;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class MongoWorldSnapshotStore implements WorldSnapshotStore {
    private final WorldSnapshotMongoRepository worldSnapshotMongoRepository;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public MongoWorldSnapshotStore(WorldSnapshotMongoRepository worldSnapshotMongoRepository) {
        this.worldSnapshotMongoRepository = worldSnapshotMongoRepository;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public void save(GameClock clock) {
        this.worldSnapshotMongoRepository.save(WorldSnapshotDocument.from(clock));
    }
}
