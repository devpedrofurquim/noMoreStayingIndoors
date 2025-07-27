package com.shavakip.nomorestayingindoor.entity;

import com.shavakip.nomorestayingindoor.world.Camera;
import com.shavakip.nomorestayingindoor.world.Position;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class Tree extends GameObject {
    private static final int VARIANT_COUNT = 3; // you have tree_0, tree_1, tree_2
    private static final BufferedImage[] TREE_IMAGES = new BufferedImage[VARIANT_COUNT];
    private static final Random RANDOM = new Random();

    static {
        for (int i = 0; i < VARIANT_COUNT; i++) {
            try {
                TREE_IMAGES[i] = ImageIO.read(Tree.class.getResource("/resources/tree_" + i + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Could not load /resources/tree_" + i + ".png: " + e.getMessage());
            }
        }
    }

    private final int variantIndex;

    public Tree(Position position) {
        super(position);
        // Each tree gets a random variant
        this.variantIndex = RANDOM.nextInt(VARIANT_COUNT);
    }

    @Override
    public void render(Graphics g, Camera camera) {
        int drawX = (int) position.x;
        int drawY = (int) position.y - 16; // if your sprite is 32px tall and you want it "standing" on the tile
        BufferedImage img = TREE_IMAGES[variantIndex];
        if (img != null) {
            g.drawImage(img, drawX, drawY, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(drawX, drawY, 16, 32);
        }
    }
}
