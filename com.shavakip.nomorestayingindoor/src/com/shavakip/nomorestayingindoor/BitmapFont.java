package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BitmapFont {
    private Font font;
    private String characters;

    public BitmapFont(String pathToTTF, float size) throws IOException, FontFormatException {
        this.font = Font.createFont(Font.TRUETYPE_FONT, new File(pathToTTF)).deriveFont(size);
    }

    public void drawString(Graphics2D g, String text, int x, int y, float scale, Color baseColor, float alpha) {
        Color colorWithAlpha = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), Math.round(alpha * 255));
        g.setColor(colorWithAlpha);
        g.setFont(font.deriveFont(font.getSize2D() * scale));
        FontMetrics fm = g.getFontMetrics();
        int cursorX = x;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            g.drawString(String.valueOf(c), cursorX, y);
            cursorX += fm.charWidth(c);
        }
    }
    public int getTextWidth(String text, float scale) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font.deriveFont(font.getSize2D() * scale));
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        g2d.dispose();
        return width;
    }
}
