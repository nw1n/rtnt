package com.example.rtnt.game.loop;

import com.example.rtnt.game.clock.domain.GameClock;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldSnapshotStore {
    void save(GameClock clock);
}
