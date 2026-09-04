package com.example.rtnt.game.core.worldsnapshot;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

@NullMarked
public interface WorldSnapshotStore {
    void save(WorldSnapshot snapshot);

    void flush();

    boolean exists(long tick);

    Optional<WorldSnapshot> findByTick(long tick);

    Optional<WorldSnapshot> findLatest();

    List<WorldSnapshot> list();
}
