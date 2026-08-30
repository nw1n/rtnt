package com.example.rtnt.game.core.loop.persistence;

import com.example.rtnt.game.core.loop.WorldSnapshot;
import com.example.rtnt.game.core.loop.WorldSnapshotStore;
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
}
