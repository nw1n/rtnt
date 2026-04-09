package com.example.rtnt.domain.player;

/**
 * Interface representing a player in the game.
 */
public interface Player {
    /**
     * Get the unique identifier of this player.
     * @return the player's ID
     */
    String getId();

    /**
     * Get the display name of this player.
     * @return the player's name
     */
    String getName();

    /**
     * Get this player's display color in hexadecimal format.
     * @return the player's hex color (e.g. #12ABEF)
     */
    String getHexColor();
}
