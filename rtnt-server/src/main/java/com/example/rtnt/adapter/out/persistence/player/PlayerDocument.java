package com.example.rtnt.adapter.out.persistence.player;

import com.example.rtnt.domain.player.Player;
import com.example.rtnt.domain.player.StandardPlayer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "players")
public class PlayerDocument {
    @Id
    private String id;
    private String name;
    private String hexColor;

    public PlayerDocument() {
    }

    public PlayerDocument(String id, String name, String hexColor) {
        this.id = id;
        this.name = name;
        this.hexColor = hexColor;
    }

    public static PlayerDocument fromDomain(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return new PlayerDocument(player.getId(), player.getName(), player.getHexColor());
    }

    public Player toDomain() {
        return StandardPlayer.fromDocument(this.id, this.name, this.hexColor);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexColor() {
        return this.hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }
}
