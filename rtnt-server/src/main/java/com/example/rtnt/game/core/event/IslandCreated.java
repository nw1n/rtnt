package com.example.rtnt.game.core.event;

import com.example.rtnt.game.island.domain.Footprint;
import com.example.rtnt.game.island.domain.Island;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record IslandCreated(
        long tick,
        String islandId,
        String name,
        int x,
        int y,
        int width,
        int length
) implements WorldEvent {
    public static final String TYPE = "IslandCreated";

    public static IslandCreated from(Island island, long tick) {
        Footprint footprint = island.footprint();
        return new IslandCreated(
                tick,
                island.id(),
                island.name(),
                footprint.x(),
                footprint.y(),
                footprint.width(),
                footprint.length()
        );
    }

    public Island toIsland() {
        return Island.existing(this.islandId, this.name, new Footprint(this.x, this.y, this.width, this.length));
    }

    @Override
    public String type() {
        return TYPE;
    }
}
