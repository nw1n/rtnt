package com.example.rtnt.game.world;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameLogEvent(long tick, String type, String detail) {
}
