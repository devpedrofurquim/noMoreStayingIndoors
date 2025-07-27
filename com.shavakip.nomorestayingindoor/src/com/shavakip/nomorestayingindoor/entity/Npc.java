package com.shavakip.nomorestayingindoor.entity;

import java.awt.Color;
import java.awt.Graphics;

import com.shavakip.nomorestayingindoor.world.Camera;
import com.shavakip.nomorestayingindoor.world.Position;

public class Npc extends GameObject {
    public Npc(Position position) {
        super(position);
    }

    @Override
    public void render(Graphics g, Camera camera) {
        // No camera subtraction, just use world position
        g.setColor(Color.BLUE);  // NPC rendered as blue square
        g.fillRect((int)position.x, (int)position.y, 16, 16);
    }
}
