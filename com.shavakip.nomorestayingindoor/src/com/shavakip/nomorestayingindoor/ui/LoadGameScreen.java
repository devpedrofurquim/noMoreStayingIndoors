package com.shavakip.nomorestayingindoor.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.shavakip.nomorestayingindoor.core.Game;
import com.shavakip.nomorestayingindoor.core.GameState;
import com.shavakip.nomorestayingindoor.graphics.BitmapFont;
import com.shavakip.nomorestayingindoor.save.SaveData;
import com.shavakip.nomorestayingindoor.save.SaveUtils;
import com.shavakip.nomorestayingindoor.world.Position;

public class LoadGameScreen {
    private final BitmapFont font;
    private final int width;
    private final int height;
    private final Game game;
    private final MenuButton[] slotButtons;
    private final MenuButton deleteButton;
    private final MenuButton[] allButtons;
    private final MenuButton backButton;
    private int selectedSlot = 0;
    private int lastSelectedSaveSlot = 0;
    private boolean deleting = false;
    

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
        backButton = slotButtons[3]; // ✅ only define it once, after setup
        deleteButton = new MenuButton("Delete Save", 1.2f);
        

        // Total buttons: 4 slots + delete
        allButtons = new MenuButton[5];
        System.arraycopy(slotButtons, 0, allButtons, 0, 4);
        allButtons[4] = deleteButton;

        allButtons[0].setSelected(true); // Default selection
    }


    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

     // Draw title or delete warning
        String title = deleting
            ? "Select a slot to delete"
            : "Select a Save Slot";

        int titleWidth = font.getTextWidth(title, 1.2f);
        Color titleColor = deleting ? Color.RED : Color.WHITE;
        font.drawString(g, title, (width - titleWidth) / 2, 30, 1.2f, titleColor, 1f);

        int spacing = 14;
        int startY = 60;

     // Render Slot 1-3
        for (int i = 0; i < 3; i++) {
            MenuButton btn = slotButtons[i];
            int btnWidth = font.getTextWidth(btn.getText(), btn.scale);
            int btnX = (width - btnWidth) / 2;
            btn.setPosition(btnX, startY, font, g);

            Color color = (btn == allButtons[selectedSlot])
                ? (deleting ? Color.RED : Color.PINK)
                : Color.WHITE;

            btn.render(g, font, 1f, color);
            startY += btn.height + spacing;
        }

        // Layout Go Back + Delete Side by Side
        backButton.setPosition(0, 0, font, g);
        deleteButton.setPosition(0, 0, font, g);

        int totalWidth = backButton.width + 30 + deleteButton.width;
        int startX = (width - totalWidth) / 2;

        backButton.setPosition(startX, startY, font, g);
        deleteButton.setPosition(startX + backButton.width + 30, startY, font, g);

        // 🟥 Color selection logic for Go Back and Delete Save
        Color backColor = (backButton == allButtons[selectedSlot])
            ? (deleting ? Color.RED : Color.PINK)
            : Color.WHITE;

        Color deleteColor = (deleteButton == allButtons[selectedSlot])
            ? (deleting ? Color.RED : Color.PINK)
            : Color.WHITE;

        backButton.render(g, font, 1f, backColor);
        deleteButton.render(g, font, 1f, deleteColor);
    }

    public void keyPressed(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
    	    allButtons[selectedSlot].setSelected(false);
    	    selectedSlot = (selectedSlot + 1) % allButtons.length;
    	    allButtons[selectedSlot].setSelected(true);
    	}

    	if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
    	    allButtons[selectedSlot].setSelected(false);
    	    selectedSlot = (selectedSlot - 1 + allButtons.length) % allButtons.length;
    	    allButtons[selectedSlot].setSelected(true);
    	}

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            select();
        }
    }

    public void mouseMoved(MouseEvent e) {
        for (int i = 0; i < allButtons.length; i++) {
            MenuButton btn = allButtons[i];
            boolean hovering = btn.contains(e.getX(), e.getY());
            btn.setSelected(hovering);
            if (hovering) selectedSlot = i;
        }
    }

    public void mouseClicked(MouseEvent e) {
        for (int i = 0; i < allButtons.length; i++) {
            if (allButtons[i].contains(e.getX(), e.getY())) {
                selectedSlot = i;
                select();
            }
        }
    }
    
    public void refreshSlots() {
        for (int i = 0; i < 3; i++) {
            if (game.saveManager.hasSave(i + 1)) {
                SaveData data = game.saveManager.loadFromSlot(i + 1);
                int percent = SaveUtils.calculateProgressPercentage(data.storyProgress);
                String label = data.saveName + " — " + percent + "%";
                slotButtons[i].setText("Slot " + (i + 1) + ": " + label);
            } else {
                slotButtons[i].setText("Slot " + (i + 1) + ": [Empty]");
            }
        }
    }

    private void select() {
        if (selectedSlot == 3) { // Go Back or Cancel
            if (deleting) {
                deleting = false;
                backButton.setText("Go Back");
            } else {
                game.getGameStateManager().setState(GameState.MAIN_MENU);
            }
        } else if (selectedSlot == 4) { // Delete Save
            deleting = true;
            backButton.setText("Cancel");
        } else {
            if (deleting) {
                int actualSlot = selectedSlot + 1;
                if (game.saveManager.hasSave(actualSlot)) {
                    game.saveManager.deleteSaveSlot(actualSlot);
                    refreshSlots();
                    deleting = false;
                    backButton.setText("Go Back");
                    System.out.println("Deleted save slot " + (selectedSlot + 1));
                }
            } else {
                int slotToLoad = selectedSlot + 1;
                if (game.saveManager.hasSave(slotToLoad)) {
                    SaveData data = game.saveManager.loadFromSlot(slotToLoad);
                    game.setCurrentSaveSlot(slotToLoad); // ✅ Track current slot
                    game.getPlayer().setPosition(new Position(data.playerX, data.playerY));
                    game.onStartGame();
                } else {
                    System.out.println("Empty slot.");
                }
                lastSelectedSaveSlot = selectedSlot;
            }
        }
    }
}
