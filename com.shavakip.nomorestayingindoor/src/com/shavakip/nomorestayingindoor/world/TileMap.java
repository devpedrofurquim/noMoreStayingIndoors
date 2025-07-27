package com.shavakip.nomorestayingindoor.world;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;
import java.io.IOException;

public class TileMap {
    public static final int TILE_SIZE = 16;  // Clearly defined public constant
    private char[][] layout;
    
    private static final int GROUND_VARIANTS = 8;
    private static BufferedImage[] groundTiles = new BufferedImage[GROUND_VARIANTS];

    private int[][] groundRandomIndexes; // stores which image index to use per tile
    
    static {
        for (int i = 0; i < GROUND_VARIANTS; i++) {
            try {
                // You may need to tweak the path if your resources are in a different folder
            	groundTiles[i] = ImageIO.read(TileMap.class.getResource("/resources/ground_" + i + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Could not load ground tile " + i + ": " + e.getMessage());
            }
        }
    }

    public TileMap(String[] mapData) {
        layout = new char[mapData.length][mapData[0].length()];
        groundRandomIndexes = new int[mapData.length][mapData[0].length()];
        Random rng = new Random();

        for (int y = 0; y < mapData.length; y++) {
            layout[y] = mapData[y].toCharArray();
            for (int x = 0; x < mapData[0].length(); x++) {
                if (layout[y][x] == '.') {
                    groundRandomIndexes[y][x] = rng.nextInt(GROUND_VARIANTS); // 0 to 7
                } else {
                    groundRandomIndexes[y][x] = -1; // Not a ground tile
                }
            }
        }
    }

    // render method stays the same, but use TILE_SIZE
    public void render(Graphics2D g, float offsetX, float offsetY) {
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                char tile = layout[y][x];
                int drawX = (int)(offsetX + x * TILE_SIZE);
                int drawY = (int)(offsetY + y * TILE_SIZE);

                switch (tile) {
                    case '#':
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                    case '.':
                        int idx = groundRandomIndexes[y][x];
                        if (idx >= 0 && groundTiles[idx] != null) {
                            g.drawImage(groundTiles[idx], drawX, drawY, null);
                        } else {
                            g.setColor(Color.BLACK);
                            g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        }
                        break;
                    case 'D':
                        g.setColor(Color.YELLOW);
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                    case 't':
                        g.setColor(new Color(34, 139, 34));
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                    case 'r':
                        g.setColor(Color.CYAN);
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                    case '~':
                        g.setColor(Color.BLUE);
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                    case 'n':
                        g.setColor(Color.PINK);
                        g.fillOval(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                }
            }
        }
    }

    
    public char[][] getLayout() {
        return layout;
    }


    public boolean isWalkable(int x, int y) {
        if (y < 0 || y >= layout.length || x < 0 || x >= layout[0].length) return false;
        char tile = layout[y][x];
        return tile == '.' || tile == 'D'; // allow floor and doors
    }
}