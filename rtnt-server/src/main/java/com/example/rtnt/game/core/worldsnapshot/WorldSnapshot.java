package com.example.rtnt.game.core.worldsnapshot;

import com.example.rtnt.game.island.domain.Island;
import com.example.rtnt.game.island.domain.IslandStatus;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public record WorldSnapshot(long tick, List<Island> islands, List<IslandStatus> islandStatuses) {
}
