package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    private final BitmapFont font;
    private final int width;
    private final int height;
    private long fadeStartTime;
    private final long fadeDuration = 1000; // ms
    private float alpha = 0.0f;

    private List<MenuButton> buttons;
    private int selectedButtonIndex = 0;
    
    private MenuActionListener listener;
    
    private MenuScreen currentScreen = MenuScreen.MAIN;


    public MainMenu(BitmapFont font, int width, int height) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.fadeStartTime = System.currentTimeMillis();
        
        buttons = new ArrayList<>();
        String[] btnTexts = {"Start Game", "Options", "Exit"};
        for (String text : btnTexts) {
            buttons.add(new MenuButton(text, 1.2f));
        }
        buttons.get(0).setSelected(true);
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - fadeStartTime;
        float progress = elapsed / (float) fadeDuration;
        alpha = Math.min(progress, 1.0f);
    }

    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        int startY = 20;
        float titleScale = 2.0f;
        int lineHeight = (int)(8 * titleScale);
        float subtitleScale = 1.0f;

        // Draw title (remove this block if you want to remove the title entirely)
        String[] titleLines = {"No More", "Staying", "Indoors"};
        for (String line : titleLines) {
            int w = font.getTextWidth(line, titleScale);
            font.drawString(g, line, (width - w) / 2, startY, titleScale, Color.WHITE, alpha);
            startY += lineHeight;
        }

        startY += 8;

        // Dynamically position and draw buttons:
        BufferedImage tmpImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tmpG = tmpImg.createGraphics();

        for (MenuButton btn : buttons) {
            int btnWidth = font.getTextWidth(btn.getText(), btn.scale);
            int btnX = (width - btnWidth) / 2;
            btn.setPosition(btnX, startY, font, tmpG);
            btn.render(g, font, alpha);
            startY += btn.height + 12; // Add vertical spacing between buttons
        }

        tmpG.dispose();
    }
    public void resetFade() {
        this.fadeStartTime = System.currentTimeMillis();
        this.alpha = 0.0f;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            buttons.get(selectedButtonIndex).setSelected(false);
            selectedButtonIndex = (selectedButtonIndex + 1) % buttons.size();
            buttons.get(selectedButtonIndex).setSelected(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            buttons.get(selectedButtonIndex).setSelected(false);
            selectedButtonIndex = (selectedButtonIndex - 1 + buttons.size()) % buttons.size();
            buttons.get(selectedButtonIndex).setSelected(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            selectButton();
        }
    }

    public void mouseMoved(MouseEvent e) {
        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            boolean hovering = btn.contains(e.getX(), e.getY());
            btn.setSelected(hovering);
            if (hovering) selectedButtonIndex = i;
        }
    }

    public void mouseClicked(MouseEvent e) {
        for (MenuButton btn : buttons) {
            if (btn.contains(e.getX(), e.getY())) {
                selectButton();
            }
        }
    }

    private void selectButton() {
        String action = buttons.get(selectedButtonIndex).getText();
        System.out.println("Selected action: " + action);

        if (listener == null) return;

        switch (currentScreen) {
            case MAIN:
                switch (action) {
                    case "Start Game":
                        currentScreen = MenuScreen.START_SUBMENU;
                        rebuildButtons("New Game", "Load Game", "Go Back");
                        break;
                    case "Options":
                        currentScreen = MenuScreen.OPTIONS_SUBMENU;
                        rebuildButtons("Image", "Sounds", "Credits", "Go Back");
                        break;
                    case "Exit":
                        listener.onExit();
                        break;
                }
                break;

            case START_SUBMENU:
                switch (action) {
                    case "New Game":
                        listener.onNewGame();
                        break;
                    case "Load Game":
                        listener.onLoadGame();
                        break;
                    case "Go Back":
                        currentScreen = MenuScreen.MAIN;
                        rebuildButtons("Start Game", "Options", "Exit");
                        break;
                }
                break;

            case OPTIONS_SUBMENU:
                switch (action) {
                    case "Image":
                        currentScreen = MenuScreen.IMAGE_SUBMENU;
                        rebuildButtons("Game Resolution", "Full Screen Toggle", "Go Back");
                        break;
                    case "Sounds":
                        currentScreen = MenuScreen.SOUND_SUBMENU;
                        rebuildButtons("Sounds FX Toggle", "Music Toggle", "Go Back");
                        break;
                    case "Credits":
                        currentScreen = MenuScreen.CREDITS_SUBMENU;
                        rebuildButtons("Show credits", "Go Back");
                        break;
                    case "Go Back":
                        currentScreen = MenuScreen.MAIN;
                        rebuildButtons("Start Game", "Options", "Exit");
                        break;
                }
                break;
            case IMAGE_SUBMENU:
                switch (action) {
                    case "Game Resolution":
                        System.out.println("Game resolution selected");
                        break;
                    case "Full Screen Toggle":
                        System.out.println("Fullscreen toggled");
                        break;
                    case "Go Back":
                        currentScreen = MenuScreen.OPTIONS_SUBMENU;
                        rebuildButtons("Image", "Sounds", "Credits", "Go Back");
                        break;
                }
                break;

            case SOUND_SUBMENU:
                switch (action) {
                    case "Sounds FX Toggle":
                        System.out.println("Sound FX toggled");
                        break;
                    case "Music Toggle":
                        System.out.println("Music toggled");
                        break;
                    case "Go Back":
                        currentScreen = MenuScreen.OPTIONS_SUBMENU;
                        rebuildButtons("Image", "Sounds", "Credits", "Go Back");
                        break;
                }
                break;

            case CREDITS_SUBMENU:
                switch (action) {
                    case "Show credits":
                        listener.onCredits(); // You can display credits screen here
                        break;
                    case "Go Back":
                        currentScreen = MenuScreen.OPTIONS_SUBMENU;
                        rebuildButtons("Image", "Sounds", "Credits", "Go Back");
                        break;
                }
                break;
        }
    }
    
    private void rebuildButtons(String... labels) {
        buttons.clear();

        int maxButtons = labels.length;
        int availableHeight = height - 60; // leave space for title
        int verticalPadding = 12;

        // Estimate: Each button height ~10px per scale unit + padding
        float maxScale = Math.min(1.8f, (float)(availableHeight / maxButtons - verticalPadding) / 10f);
        float finalScale = Math.max(0.9f, Math.min(1.2f, maxScale)); // clamp between 0.9 and 1.2

        for (String label : labels) {
            buttons.add(new MenuButton(label, finalScale));
        }

        selectedButtonIndex = 0;
        if (!buttons.isEmpty()) {
            buttons.get(0).setSelected(true);
        }
    }
    
    public void setMenuActionListener(MenuActionListener listener) {
        this.listener = listener;
    }
    
    private void renderMainMenu(Graphics2D g) {
        String[] mainButtons = {"Start Game", "Options", "Exit"};
        // Render those buttons
    }
    private void renderStartSubMenu(Graphics2D g) {
        String[] startButtons = {"New Game", "Load Game", "← Back"};
        // Render those buttons
    }
}
