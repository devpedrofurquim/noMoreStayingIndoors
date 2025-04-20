package com.shavakip.nomorestayingindoor.ui;

import java.awt.*;

import com.shavakip.nomorestayingindoor.graphics.BitmapFont;

public class PauseScreen {
    private final BitmapFont font;
    private final int width;
    private final int height;
    private float alpha = 0.0f;
    private long fadeStartTime;
    private final long fadeDuration = 500;

    public PauseScreen(BitmapFont font, int width, int height) {
        this.font = font;
        this.width = width;
        this.height = height;
    }
    
    public void resetFade() {
        this.fadeStartTime = System.currentTimeMillis();
        this.alpha = 0.0f;
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - fadeStartTime;
        float progress = elapsed / (float) fadeDuration;
        this.alpha = Math.min(progress, 1.0f);
    }

    public void render(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);

        String text = "PAUSED - Press P to Resume";
        float scale = 1.0f;
        int textWidth = font.getTextWidth(text, scale);
        int textX = (width - textWidth) / 2;
        int textY = height / 2;

        font.drawString(g, text, textX, textY, scale, Color.WHITE, alpha);
    }
}
