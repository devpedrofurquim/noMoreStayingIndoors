package com.shavakip.nomorestayingindoor;

import java.awt.*;

public class SavePopup {
    private final BitmapFont font;
    private String message = null;
    private long startTime = 0;
    private final long DURATION = 2500;

    public SavePopup(BitmapFont font) {
        this.font = font;
    }

    public void show(String message) {
        this.message = message;
        this.startTime = System.currentTimeMillis();
    }

    public void render(Graphics2D g, int canvasWidth, int canvasHeight) {
        if (message == null) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > DURATION) {
            message = null;
            return;
        }

        float alpha = 1.0f - (elapsed / (float) DURATION);
        alpha = Math.max(0f, Math.min(1f, alpha));

        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        float scale = 2.5f;
        int textWidth = font.getTextWidth(message, scale);
        int textHeight = font.getLineHeight(g, scale);

        int padding = 20;
        int boxWidth = textWidth + padding * 2;
        int boxHeight = textHeight + padding;

        int x = (canvasWidth - boxWidth) / 2;
        int y = (int)(canvasHeight * 0.2f); // 20% from top

        // Background
        g.setColor(new Color(0, 0, 0, (int)(200 * alpha)));
        g.fillRoundRect(x, y, boxWidth, boxHeight, 20, 20);

        // Text centered in box
        int textX = x + (boxWidth - textWidth) / 2;
        int textY = y + (boxHeight + textHeight) / 2 - 6;
        font.drawString(g, message, textX, textY, scale, Color.WHITE, 1f);

        g.setComposite(original);
    }
}
