package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Particle {
    private int x, y;
    private BufferedImage frame;
    private float alpha = 0.4f; // lowered starting opacity
    private float fadeSpeed = 0.05f;

    public Particle(int x, int y, BufferedImage frame) {
        this.x = x;
        this.y = y;
        this.frame = frame;	
    }

    public boolean isDead() {
        return alpha <= 0;
    }

    public void update(double deltaTime) {
        alpha -= fadeSpeed;
        if (alpha < 0) alpha = 0;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int scale = 1;
        g2d.drawImage(frame, x, y, 16 * scale, 16 * scale, null);
        g2d.setComposite(old);
    }
}
