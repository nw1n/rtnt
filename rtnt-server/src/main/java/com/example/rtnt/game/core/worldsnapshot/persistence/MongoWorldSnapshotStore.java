package com.example.rtnt.game.core.worldsnapshot.persistence;

import com.example.rtnt.game.core.worldsnapshot.WorldSnapshot;
import com.example.rtnt.game.core.worldsnapshot.WorldSnapshotStore;
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
    public void save(WorldSnapshot snapshot) {
        this.worldSnapshotMongoRepository.save(WorldSnapshotDocument.from(snapshot));
    }

    @Override
    public boolean exists(long tick) {
        return this.worldSnapshotMongoRepository.existsById(tick);
    }
}
