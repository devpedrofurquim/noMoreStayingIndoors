package com.shavakip.nomorestayingindoor.save;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String saveName;
    public float playerX;
    public float playerY;
    public int storyProgress;
    public Map<String, String> playerChoices;

    public SaveData(String saveName, float playerX, float playerY, int storyProgress, Map<String, String> playerChoices) {
        this.saveName = saveName;
        this.playerX = playerX;
        this.playerY = playerY;
        this.storyProgress = storyProgress;
        this.playerChoices = playerChoices;
    }

    // Default constructor for initial save data
    public SaveData(String saveName, float playerX, float playerY) {
        this(saveName, playerX, playerY, 0, new HashMap<>()); // Initialize with defaults
    }
    
    public void setChoice(String key, String value) {
        if (playerChoices == null) {
            playerChoices = new HashMap<>();
        }
        playerChoices.put(key, value);
    }

    public String getChoice(String key) {
        if (playerChoices == null) return null;
        return playerChoices.get(key);
    }

    public boolean hasChoice(String key) {
        return playerChoices != null && playerChoices.containsKey(key);
    }
}
