package com.shavakip.nomorestayingindoor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CreditsScreen {

    private final BitmapFont font;
    private final int width;
    private final int height;
    private final String[] lines = {
        "No More Staying Indoors",
        "",
        "Developed with love by:",
        "Pedro Furquim",
        "",
        "Special Thanks:",
        "Eric Barone",
        "Toby Fox",
        "Alec Holowkwa",
        "& Java",
        "",
        "Thanks for playing!",
        "",
        "2025"
    };

    private final float scale = 1.2f;
    private final int lineHeight = (int) (10 * scale + 6);
    private final int totalHeight = lines.length * lineHeight;
    private final long duration;
    private final long fadeDuration;

    public CreditsScreen(BitmapFont font, int width, int height, long duration, long fadeDuration) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.fadeDuration = fadeDuration;
    }

    public void render(Graphics2D g, long elapsed, float fadeAlpha) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        float scrollProgress = elapsed / (float) duration;
        scrollProgress = (float) Math.pow(scrollProgress, 0.8f); // ease-in

        float scrollSpeedFactor = 1.0f;
        int scrollRange = (int) ((height + totalHeight) * scrollSpeedFactor);
        int scrollY = (int) (height - scrollProgress * scrollRange);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int textWidth = font.getTextWidth(line, scale);
            font.drawString(g, line, (width - textWidth) / 2, scrollY + i * lineHeight, scale, Color.WHITE, Math.max(fadeAlpha, 0.01f));
        }
    }
}
