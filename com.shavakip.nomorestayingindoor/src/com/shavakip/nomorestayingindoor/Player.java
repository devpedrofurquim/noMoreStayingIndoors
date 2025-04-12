package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;

public class Player extends GameObject {
    private float velocityX = 0;
    private float velocityY = 0;

    private java.util.List<Particle> particles = new ArrayList<>();
    private BufferedImage[][] animations; // animations[direction][frame]

    private int currentFrame = 0;
    private long lastFrameTime;
    private int frameDelay = 150;

    private final float speed = 30;

    private enum Direction {
        IDLE, DOWN, RIGHT, LEFT, UP
    }

    private Direction currentDirection = Direction.IDLE;

    public Player(Position startPosition) {
		super(startPosition); // Calls GameObject's constructor

        try {
            BufferedImage tileset = ImageIO.read(new File("res/tileset.png"));

            animations = new BufferedImage[5][7]; // 5 directions, 7 frames each

            for (int i = 0; i < 7; i++) {
                animations[Direction.IDLE.ordinal()][i] = tileset.getSubimage(i * 16, 0 * 16, 16, 16);
                animations[Direction.DOWN.ordinal()][i] = tileset.getSubimage(i * 16, 1 * 16, 16, 16);
                animations[Direction.RIGHT.ordinal()][i] = tileset.getSubimage(i * 16, 2 * 16, 16, 16);
                animations[Direction.LEFT.ordinal()][i] = tileset.getSubimage(i * 16, 3 * 16, 16, 16);
                animations[Direction.UP.ordinal()][i] = tileset.getSubimage(i * 16, 4 * 16, 16, 16);
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

        BufferedImage[] anim = animations[currentDirection.ordinal()];
        g.drawImage(anim[currentFrame], (int) position.x, (int) position.y, 16, 16, null);
    }

    public void setVelocity(float vx, float vy) {
        this.velocityX = vx * speed;
        this.velocityY = vy * speed;

        if (vx == 0 && vy == 0) {
            currentDirection = Direction.IDLE;
        } else if (Math.abs(vx) > Math.abs(vy)) {
            currentDirection = vx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            currentDirection = vy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    public void update(double deltaTime) {
        BufferedImage[] anim = animations[currentDirection.ordinal()];

        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= frameDelay) {
            currentFrame = (currentFrame + 1) % anim.length;
            lastFrameTime = now;

            if (isMoving()) {
                particles.add(new Particle((int) position.x, (int) position.y, anim[currentFrame]));
            }
        }

        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            p.update(deltaTime);
            if (p.isDead()) iter.remove();
        }

        position.x += velocityX * deltaTime;
        position.y += velocityY * deltaTime;
    }

    private boolean isMoving() {
        return velocityX != 0 || velocityY != 0;
    }
}
