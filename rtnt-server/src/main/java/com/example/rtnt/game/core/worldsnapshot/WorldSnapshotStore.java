package com.example.rtnt.game.core.worldsnapshot;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldSnapshotStore {
    void save(WorldSnapshot snapshot);

    boolean exists(long tick);
}
