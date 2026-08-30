package com.example.rtnt.game.core.loop;

import com.example.rtnt.game.island.domain.Island;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public record WorldSnapshot(long tick, List<Island> islands) {
}
