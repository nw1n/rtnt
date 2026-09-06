package com.example.rtnt.game.core.event;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record WorldCleared(long tick) implements WorldEvent {
    public static final String TYPE = "WorldCleared";

    @Override
    public String type() {
        return TYPE;
    }
}
