package com.shavakip.nomorestayingindoor;

import java.awt.*;

public class MainMenu {
    private final BitmapFont font;
    private final int width;
    private final int height;
    private long fadeStartTime;
    private final long fadeDuration = 1000; // ms
    private float alpha = 0.0f;

    public MainMenu(BitmapFont font, int width, int height) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.fadeStartTime = System.currentTimeMillis();
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - fadeStartTime;
        float progress = elapsed / (float) fadeDuration;
        alpha = Math.min(progress, 1.0f);
    }

    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        String[] titleLines = {"No More", "Staying", "Indoors"};
        float titleScale = 2.0f;
        float subtitleScale = 1.0f;
        int lineHeight = (int)(8 * titleScale);
        int startY = 20;

        for (String line : titleLines) {
            int w = font.getTextWidth(line, titleScale);
            font.drawString(g, line, (width - w) / 2, startY, titleScale, Color.WHITE, alpha);
            startY += lineHeight;
        }
        startY += 4;

        font.drawString(g, "by pedro furquim",
                (width - font.getTextWidth("by pedro furquim", subtitleScale)) / 2,
                startY, subtitleScale, Color.LIGHT_GRAY, alpha);

        startY += (int)(8 * subtitleScale) + 4;

        font.drawString(g, "Press ENTER to Start",
                (width - font.getTextWidth("Press ENTER to Start", subtitleScale)) / 2,
                startY, subtitleScale, Color.GRAY, alpha);
    }

    public void resetFade() {
        this.fadeStartTime = System.currentTimeMillis();
        this.alpha = 0.0f;
    }
}
