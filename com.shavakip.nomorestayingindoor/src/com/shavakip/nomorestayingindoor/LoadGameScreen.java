package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class LoadGameScreen {
    private final BitmapFont font;
    private final int width;
    private final int height;
    private final Game game;
    private final MenuButton[] slotButtons;
    private int selectedSlot = 0;

    public LoadGameScreen(BitmapFont font, int width, int height, Game game) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.game = game;

        slotButtons = new MenuButton[4]; // 3 saves + Go Back

        for (int i = 0; i < 3; i++) {
            String label = game.saveManager.hasSave(i + 1)
                ? "Slot " + (i + 1) + ": " + game.saveManager.loadFromSlot(i + 1).saveName
                : "Slot " + (i + 1) + ": [Empty]";
            slotButtons[i] = new MenuButton(label, 1.2f);
        }

        slotButtons[3] = new MenuButton("Go Back", 1.2f);
        slotButtons[selectedSlot].setSelected(true);
    }

    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        int startY = 80;

        for (int i = 0; i < slotButtons.length; i++) {
            MenuButton btn = slotButtons[i];
            int btnWidth = font.getTextWidth(btn.getText(), btn.scale);
            int btnX = (width - btnWidth) / 2;
            btn.setPosition(btnX, startY, font, g);
            btn.render(g, font, 1f);
            startY += btn.height + 16;
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            slotButtons[selectedSlot].setSelected(false);
            selectedSlot = (selectedSlot + 1) % slotButtons.length;
            slotButtons[selectedSlot].setSelected(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            slotButtons[selectedSlot].setSelected(false);
            selectedSlot = (selectedSlot - 1 + slotButtons.length) % slotButtons.length;
            slotButtons[selectedSlot].setSelected(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            select();
        }
    }

    public void mouseMoved(MouseEvent e) {
        for (int i = 0; i < slotButtons.length; i++) {
            MenuButton btn = slotButtons[i];
            boolean hovering = btn.contains(e.getX(), e.getY());
            btn.setSelected(hovering);
            if (hovering) selectedSlot = i;
        }
    }

    public void mouseClicked(MouseEvent e) {
        for (int i = 0; i < slotButtons.length; i++) {
            if (slotButtons[i].contains(e.getX(), e.getY())) {
                selectedSlot = i;
                select();
            }
        }
    }
    
    public void refreshSlots() {
        for (int i = 0; i < 3; i++) {
            if (game.saveManager.hasSave(i + 1)) {
                SaveData data = game.saveManager.loadFromSlot(i + 1);
                slotButtons[i].setText("Slot " + (i + 1) + ": " + data.saveName);
            } else {
                slotButtons[i].setText("Slot " + (i + 1) + ": [Empty]");
            }
        }
    }

    private void select() {
        if (selectedSlot == 3) {
        	game.getGameStateManager().setState(GameState.MAIN_MENU);
        } else {
            if (game.saveManager.hasSave(selectedSlot + 1)) {
                SaveData data = game.saveManager.loadFromSlot(selectedSlot + 1);
                game.getPlayer().setPosition(new Position(data.playerX, data.playerY));
                game.onStartGame();
            } else {
                // Optional: feedback like "Empty slot"
                System.out.println("Empty slot.");
            }
        }
    }
}
