package com.example.rtnt.game.trade.domain;

import com.example.rtnt.game.island.domain.IslandStatus;
import com.example.rtnt.game.ship.domain.Ship;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public record TradeResult(Ship ship, IslandStatus islandStatus, List<TradeEvent> events) {
}
