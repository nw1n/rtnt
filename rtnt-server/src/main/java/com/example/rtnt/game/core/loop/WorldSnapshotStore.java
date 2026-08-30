package com.example.rtnt.game.core.loop;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldSnapshotStore {
    void save(WorldSnapshot snapshot);
}
