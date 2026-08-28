package com.example.rtnt.game.log.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameLogEvent(long tick, String type, String detail) {
}
