package com.shavakip.nomorestayingindoor.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.shavakip.nomorestayingindoor.core.Game;
import com.shavakip.nomorestayingindoor.graphics.BitmapFont;

public class PauseScreen {

    private final BitmapFont font;
    private final int width;
    private final int height;
    private final List<MenuButton> buttons = new ArrayList<>();

    private int selectedIndex = 0;
    private BufferedImage backgroundSnapshot;

    public PauseScreen(BitmapFont font, int width, int height) {
        this.font = font;
        this.width = width;
        this.height = height;

        String[] labels = {"Resume", "Save Game", "Settings", "Quit to Menu"};
        for (String label : labels) {
            buttons.add(new MenuButton(label, 1.2f));
        }
        buttons.get(0).setSelected(true);
    }

    public void setBackgroundSnapshot(BufferedImage snapshot) {
        this.backgroundSnapshot = snapshot;
    }

    public void update() {
        // Optional fade or animation
    }

    public void render(Graphics2D g) {
        // ==== Fake blur effect ====
    	if (backgroundSnapshot != null) {
    		g.drawImage(
    			    backgroundSnapshot,
    			    0, 0, width, height,  // destination rectangle on screen
    			    0, 0, backgroundSnapshot.getWidth(), backgroundSnapshot.getHeight(),  // source rectangle from snapshot
    			    null
    			);    	}

        // ==== Frosted glass overlay ====
        g.setColor(new Color(40, 30, 30, 160));
        g.fillRoundRect(30, 20, width - 60, height - 40, 20, 20);

        // Title config
        String title = "PAUSED";
        float titleScale = 2f;
        int titleHeight = (int)(font.getLineHeight(g, titleScale));
        int titleWidth = font.getTextWidth(title, titleScale);

        // Button config
        float buttonScale = 1.2f;
        int spacing = 16;
        int buttonHeight = (int)(font.getLineHeight(g, buttonScale));
        int totalButtonHeight = buttons.size() * (buttonHeight + spacing) - spacing;

        // Combined block height
        int totalHeight = titleHeight + spacing + totalButtonHeight;
        int startY = (height - totalHeight) / 2;

        // Render title
        font.drawString(g, title, (width - titleWidth) / 2, startY, titleScale, Color.WHITE, 1f);

        // Render buttons
        BufferedImage tmpImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempG = tmpImg.createGraphics();

        int currentY = startY + titleHeight + spacing;
        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            btn.setSelected(i == selectedIndex);
            int btnWidth = font.getTextWidth(btn.getText(), btn.scale);
            int btnX = (width - btnWidth) / 2;
            btn.setPosition(btnX, currentY, font, tempG);
            btn.render(g, font, 1f, null);
            currentY += btn.height + spacing;
        }

        tempG.dispose();
    }

    public void keyPressed(KeyEvent e, Game game) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            selectedIndex = (selectedIndex - 1 + buttons.size()) % buttons.size();
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            selectedIndex = (selectedIndex + 1) % buttons.size();
        } else if (key == KeyEvent.VK_ENTER) {
            String action = buttons.get(selectedIndex).getText();

            switch (action) {
                case "Resume":
                    game.getGameStateManager().setState(com.shavakip.nomorestayingindoor.core.GameState.PLAYING);
                    break;
                case "Save Game":
                    game.quickSave("pauseSave", "true");
                    break;
                case "Settings":
                    game.onOptions(); // placeholder
                    break;
                case "Quit to Menu":
                    game.getGameStateManager().setState(com.shavakip.nomorestayingindoor.core.GameState.MAIN_MENU);
                    break;
            }
        }
    }

    public void resetFade() {
        // Optional fade animation
    }

    public void mouseMoved(MouseEvent e) {
        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            boolean hovering = btn.contains(e.getX(), e.getY());
            btn.setSelected(hovering);
            if (hovering) selectedIndex = i;
        }
    }

    public void mouseClicked(MouseEvent e, Game game) {
        for (MenuButton btn : buttons) {
            if (btn.contains(e.getX(), e.getY())) {
                keyPressed(new KeyEvent(e.getComponent(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n'), game);
            }
        }
    }
}