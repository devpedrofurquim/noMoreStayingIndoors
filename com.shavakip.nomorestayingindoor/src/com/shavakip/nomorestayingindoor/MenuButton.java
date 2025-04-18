package com.shavakip.nomorestayingindoor;

import java.awt.*;

public class MenuButton {
    private String text;
    public int x, y, width, height;
    public float scale;
    private boolean selected = false;

    public MenuButton(String text, float scale) {
        this.text = text;
        this.scale = scale;
    }

    public void setPosition(int x, int y, BitmapFont font, Graphics2D g) {
        this.x = x;
        this.y = y;
        g.setFont(font.getFont().deriveFont(font.getFont().getSize2D() * scale));
        FontMetrics fm = g.getFontMetrics();
        this.width = fm.stringWidth(text);
        this.height = fm.getHeight();
    }

    public void render(Graphics2D g, BitmapFont font, float alpha) {
        Color color = selected ? Color.YELLOW : Color.WHITE;
        font.drawString(g, text, x, y, scale, color, alpha);
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= (y - height) && my <= y;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getText() {
        return text;
    }
}
