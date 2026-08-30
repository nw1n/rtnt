package com.example.rtnt.game.core.worldsnapshot;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

@NullMarked
public interface WorldSnapshotStore {
    void save(WorldSnapshot snapshot);

    boolean exists(long tick);

    Optional<WorldSnapshot> findByTick(long tick);

    List<WorldSnapshot> list();
}
