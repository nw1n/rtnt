package com.example.rtnt.game.core.worldsnapshot;

import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public interface WorldSnapshotStore {
    void save(WorldSnapshot snapshot);

    boolean exists(long tick);

    List<WorldSnapshot> list();
}
