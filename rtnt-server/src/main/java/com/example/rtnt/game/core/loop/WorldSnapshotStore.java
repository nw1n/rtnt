package com.example.rtnt.game.loop;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldSnapshotStore {
    void save(WorldSnapshot snapshot);
}
