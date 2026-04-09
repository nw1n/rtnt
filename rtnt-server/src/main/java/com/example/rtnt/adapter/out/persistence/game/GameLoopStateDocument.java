package com.example.rtnt.adapter.out.persistence.game;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_loop_state")
public class GameLoopStateDocument {
    @Id
    private String id;
    private long currentTick;

    public GameLoopStateDocument() {
    }

    public GameLoopStateDocument(String id, long currentTick) {
        this.id = id;
        this.currentTick = currentTick;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCurrentTick() {
        return this.currentTick;
    }

    public void setCurrentTick(long currentTick) {
        this.currentTick = currentTick;
    }
}
