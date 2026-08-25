package com.example.rtnt.domain.island;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Footprint(
        @PositiveOrZero int x,
        @PositiveOrZero int y,
        @Positive int width,
        @Positive int length
) {
}
