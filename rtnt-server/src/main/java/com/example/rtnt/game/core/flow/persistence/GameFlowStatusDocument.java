package com.example.rtnt.game.core.flow.persistence;

import com.example.rtnt.game.core.flow.FlowMode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_flow_status")
@NullMarked
public record GameFlowStatusDocument(
        @Id String id,
        FlowMode mode,
        boolean paused,
        @Nullable Integer liveIntervalMs
) {
    public static final String DOCUMENT_ID = "default";
    public static final int DEFAULT_LIVE_INTERVAL_MS = 1000;

    /***************************************************************************
     *                                                                         *
     * Static Factory Methods                                                  *
     *                                                                         *
     **************************************************************************/

    public static GameFlowStatusDocument from(FlowMode mode, boolean paused, int liveIntervalMs) {
        return new GameFlowStatusDocument(DOCUMENT_ID, mode, paused, liveIntervalMs);
    }

    public int resolvedLiveIntervalMs(int fallback) {
        if (this.liveIntervalMs == null || this.liveIntervalMs < 1) {
            return fallback;
        }
        return this.liveIntervalMs;
    }
}
