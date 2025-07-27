package com.shavakip.nomorestayingindoor.world;

import java.awt.Color;
import java.awt.Graphics2D;

public class Door {
    private float x, y, width, height;
    private String targetMapId;
    private float targetX, targetY;
    private float exitOffsetX = 0, exitOffsetY = 0;

    public Door(float x, float y, float width, float height, String targetMapId, float targetX, float targetY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetMapId = targetMapId;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public Door(float x, float y, float width, float height, String targetMapId, float targetX, float targetY, float exitOffsetX, float exitOffsetY) {
        this(x, y, width, height, targetMapId, targetX, targetY);
        this.exitOffsetX = exitOffsetX;
        this.exitOffsetY = exitOffsetY;
    }

    public boolean intersects(Position pos) {
        return pos.getX() + 8 > x && pos.getX() < x + width &&
               pos.getY() + 8 > y && pos.getY() < y + height;
    }

    public String getTargetMapId() { return targetMapId; }
    public float getTargetX() { return targetX; }
    public float getTargetY() { return targetY; }

    public float getExitOffsetX() { return exitOffsetX; }
    public float getExitOffsetY() { return exitOffsetY; }

    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void render(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.drawRect((int)x, (int)y, (int)width, (int)height);
    }
}
