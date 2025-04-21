package com.shavakip.nomorestayingindoor.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

import com.shavakip.nomorestayingindoor.core.Game;
import com.shavakip.nomorestayingindoor.graphics.BitmapFont;

public class IntroScreen {

    private List<String> lines;
    private int currentLine = 0;
    private long lastChangeTime = 0;
    private long delay = 2000;
    private float alpha = 0.0f;
    private boolean fadingIn = true;
    private boolean finished = false;

    private boolean showingOutro = false;
    private float outroAlpha = 0f;
    private long outroStartTime = 0;
    private final String outroText = "No More Staying Indoors, a game by Pedro Furquim";
    private final long OUTRO_DURATION = 4000;

    private boolean ePressed = false;
    private long ePressedStart = 0;

    private final Game game;
    private final String saveName;
    private final BitmapFont font;
    private final int width, height;

    private final long SKIP_HOLD_TIME = 5000;

    // === Sprite animation fields ===
    private BufferedImage spriteSheet;
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private final int FRAME_WIDTH = 64;
    private final int FRAME_HEIGHT = 64;
    private final int TOTAL_FRAMES = 6;
    private final int FRAME_DURATION = 120; // ms

    public IntroScreen(Game game, BitmapFont font, int width, int height, String saveName) {
        this.game = game;
        this.saveName = saveName;
        this.font = font;
        this.width = width;
        this.height = height;

        lines = loadLinesFromFile("res/intro_poem.txt");

        try {
            spriteSheet = ImageIO.read(new File("res/frame_1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> loadLinesFromFile(String path) {
        try {
            return java.nio.file.Files.readAllLines(java.nio.file.Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return List.of("Could not load intro text.");
        }
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (finished) return;

        // Animate sprite
        if (!showingOutro && now - lastFrameTime >= FRAME_DURATION) {
            frameIndex = (frameIndex + 1) % TOTAL_FRAMES;
            lastFrameTime = now;
        }

        // Skip logic
        if (ePressed && now - ePressedStart >= SKIP_HOLD_TIME) {
            finished = true;
            game.onStartGame();
            return;
        }

        if (showingOutro) {
            if (outroAlpha < 1.0f) {
                outroAlpha += 0.02f;
                if (outroAlpha > 1.0f) outroAlpha = 1.0f;
            } else if (now - outroStartTime >= OUTRO_DURATION) {
                finished = true;
                game.onStartGame();
            }
            return;
        }

        if (fadingIn) {
            alpha += 0.02f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadingIn = false;
                lastChangeTime = now;
            }
        } else if (now - lastChangeTime > delay) {
            nextLine();
        }
    }

    public void render(Graphics2D g) {
        if (finished) return;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

     // === Sprite Animation ===
        if (spriteSheet != null) {
            int sx = frameIndex * FRAME_WIDTH;
            int sy = 0;

            int SPRITE_SCALE = 2;
            int drawW = FRAME_WIDTH * SPRITE_SCALE;
            int drawH = FRAME_HEIGHT * SPRITE_SCALE;
            int drawX = (width - drawW) / 2;
            int drawY = height / 4 - drawH / 2; // place at 1/4 of screen vertically

            g.drawImage(spriteSheet, drawX, drawY, drawX + drawW, drawY + drawH,
                        sx, sy, sx + FRAME_WIDTH, sy + FRAME_HEIGHT, null);
        }

     // === Poem Line (above animation) ===
        if (!showingOutro && currentLine < lines.size()) {
            String line = lines.get(currentLine);
            float scale = 1.2f;
            int textWidth = font.getTextWidth(line, scale);
            int lineHeight = font.getLineHeight(g, scale);

            int SPRITE_SCALE = 2;
            int y = height / 8; // 1/8 of the screen height — safely above the sprite


            int x = (width - textWidth) / 2;
            font.drawString(g, line, x, y, scale, Color.WHITE, alpha);
        }

        // === Outro ===
        if (showingOutro) {
            float outroScale = 1.2f;

            String line1 = "No More Staying Indoors";
            String line2 = "a game by Pedro Furquim";

            int line1Width = font.getTextWidth(line1, outroScale);
            int line2Width = font.getTextWidth(line2, outroScale);
            int lineHeight = font.getLineHeight(g, outroScale);

            int line1X = (width - line1Width) / 2;
            int line2X = (width - line2Width) / 2;

            int baseY = height / 2;

            font.drawString(g, line1, line1X + 1, baseY + 1, outroScale, Color.BLACK, outroAlpha * 0.7f);
            font.drawString(g, line2, line2X + 1, baseY + lineHeight + 1, outroScale, Color.BLACK, outroAlpha * 0.7f);

            font.drawString(g, line1, line1X, baseY, outroScale, Color.WHITE, outroAlpha);
            font.drawString(g, line2, line2X, baseY + lineHeight, outroScale, Color.WHITE, outroAlpha);
        }

        // === Hold-to-Skip Bar ===
        float barWidth = 60;
        float barHeight = 6;
        float padding = 10;
        int barX = width - (int) barWidth - (int) padding;
        int barY = height - (int) barHeight - (int) padding;

        float progress = 0f;
        if (ePressed) {
            long heldTime = System.currentTimeMillis() - ePressedStart;
            progress = Math.min(1f, heldTime / (float) SKIP_HOLD_TIME);
        }

        g.setColor(new Color(255, 255, 255, 120));
        g.fillRect(barX, barY, (int) (barWidth * progress), (int) barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, (int) barWidth, (int) barHeight);

        font.drawString(g, "Hold [E] to skip", barX - 110, barY + 6, 1.2f, Color.WHITE, 0.5f);
    }

    private void nextLine() {
        alpha = 0.0f;
        fadingIn = true;
        lastChangeTime = System.currentTimeMillis();
        currentLine++;

        if (currentLine >= lines.size()) {
            showingOutro = true;
            outroAlpha = 0f;
            outroStartTime = System.currentTimeMillis();
        }
    }

    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_E && !ePressed) {
            ePressed = true;
            ePressedStart = System.currentTimeMillis();
        }
    }

    public void keyReleased(int keyCode) {
        if (keyCode == KeyEvent.VK_E) {
            ePressed = false;
        }
    }
}
