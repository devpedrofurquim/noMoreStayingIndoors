package com.shavakip.nomorestayingindoor;

import java.io.Serializable;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String saveName;
    public float playerX;
    public float playerY;

    public SaveData(String saveName, float playerX, float playerY) {
        this.saveName = saveName;
        this.playerX = playerX;
        this.playerY = playerY;
    }
}
