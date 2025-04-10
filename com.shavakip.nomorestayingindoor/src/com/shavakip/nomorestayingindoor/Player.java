package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.Iterator;

public class Player {
	private float x, y;
	private float velocityX = 0;
	private float velocityY = 0;
	
	private java.util.List<Particle> particles = new ArrayList<>();

    private BufferedImage sprite;
    
    BufferedImage[] ghostFrames;
    int currentFrame = 0;
    long lastFrameTime;
    int frameDelay = 150; // ms between frames
    
    private long lastParticleTime = 0;
    private int particleDelay = 120; // ms between particles — increase to reduce frequency
    
    private final float speed = 30f; // pixels per second
    
    

    public Player(float x, float y) {
        this.x = x;
        this.y = y;

        try {
            BufferedImage tileset = ImageIO.read(new File("res/tileset.png"));
            
            ghostFrames = new BufferedImage[6]; // adjust frame count as needed
            for (int i = 0; i < ghostFrames.length; i++) {
                ghostFrames[i] = tileset.getSubimage(i * 16, 208, 16, 16);
            }

            lastFrameTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(Graphics g) {
    	for (Particle p : particles) {
    	    p.render(g);
    	}
        int scale = 1;
        g.drawImage(ghostFrames[currentFrame], (int)x, (int)y, 16 * scale, 16 * scale, null);
    }
    public void setVelocity(float vx, float vy) {
        this.velocityX = vx * speed;
        this.velocityY = vy * speed;
    }
    
    public void update(double deltaTime) {
        if (velocityX != 0 || velocityY != 0) {
            long now = System.currentTimeMillis();
            if (now - lastFrameTime >= frameDelay) {
                particles.add(new Particle((int) x, (int) y, ghostFrames[currentFrame]));
                currentFrame = (currentFrame + 1) % ghostFrames.length;
                lastFrameTime = now;
            }
        } else {
            currentFrame = 0;
        }

        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            p.update(deltaTime);
            if (p.isDead()) iter.remove();
        }

        // ✅ Frame-rate independent movement
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
    }
}
