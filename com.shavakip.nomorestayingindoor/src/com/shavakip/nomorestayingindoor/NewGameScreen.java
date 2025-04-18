package com.shavakip.nomorestayingindoor;

import java.awt.*;
import java.awt.event.KeyEvent;

public class NewGameScreen {
    private StringBuilder saveName = new StringBuilder();
    private BitmapFont font;
    private int width, height;
    private Game game;
    private boolean showCursor = true;
    private long lastBlinkTime = 0;
    private final long BLINK_INTERVAL = 500;

    private MenuButton okButton;
    private MenuButton backButton;
    private MenuButton[] buttons;
    private int selectedIndex = -1;

    private String errorMessage = "";

    public NewGameScreen(BitmapFont font, int width, int height, Game game) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.game = game;

        okButton = new MenuButton("OK", 1.0f);
        backButton = new MenuButton("GO BACK", 1.0f);
        buttons = new MenuButton[]{backButton, okButton};
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if ((Character.isLetterOrDigit(c) || Character.isWhitespace(c)) && saveName.length() < 16 && selectedIndex == -1) {
            saveName.append(c);
            errorMessage = "";
        } else if (c == '\b' && saveName.length() > 0 && selectedIndex == -1) {
            saveName.deleteCharAt(saveName.length() - 1);
            errorMessage = "";
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_RIGHT) {
            if (selectedIndex < buttons.length - 1) {
                selectedIndex++;
                updateButtonSelection();
            }
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_LEFT) {
            if (selectedIndex > -1) {
                selectedIndex--;
                updateButtonSelection();
            }
        } else if (key == KeyEvent.VK_ENTER) {
            if (selectedIndex == -1) {
                // Typing mode: try to start
                tryStartGame();
            } else {
                // Button mode
                if (buttons[selectedIndex] == okButton) {
                    tryStartGame();
                } else if (buttons[selectedIndex] == backButton) {
                	game.getGameStateManager().setState(GameState.MAIN_MENU);
                }
            }
        }
    }

    public void tick(double deltaTime) {
        long now = System.currentTimeMillis();
        if (now - lastBlinkTime >= BLINK_INTERVAL) {
            showCursor = !showCursor;
            lastBlinkTime = now;
        }
    }

    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // Title
        String title = "Name your save:";
        int titleWidth = font.getTextWidth(title, 1.2f);
        font.drawString(g, title, (width - titleWidth) / 2, 60, 1.2f, Color.WHITE, 1f);

        // Text input
        String inputText = saveName.toString() + (showCursor && selectedIndex == -1 ? "|" : "");
        int inputWidth = font.getTextWidth(inputText, 1.0f);
        font.drawString(g, inputText, (width - inputWidth) / 2, 100, 1.2f, Color.PINK, 1f);

        // Error message
        if (!errorMessage.isEmpty()) {
            int errorWidth = font.getTextWidth(errorMessage, 0.9f);
            font.drawString(g, errorMessage, (width - errorWidth) / 2, 130, 1.2f, Color.RED, 1f);
        }

        // Position buttons
        int spacing = 20;
        int buttonY = 150;

        // Pre-measure widths
        backButton.setPosition(0, 0, font, g);  // measure width
        okButton.setPosition(0, 0, font, g);    // measure width

        int totalWidth = backButton.width + spacing + okButton.width;
        int startX = (width - totalWidth) / 2;

        // Final positions (back goes first!)
        backButton.setPosition(startX, buttonY, font, g);
        okButton.setPosition(startX + backButton.width + spacing, buttonY, font, g);

        // Render buttons
        backButton.render(g, font, 1f);
        okButton.render(g, font, 1f);
    }

    public void mouseMoved(Point p) {
        boolean anyHovered = false;
        for (int i = 0; i < buttons.length; i++) {
            MenuButton btn = buttons[i];
            if (btn.contains(p.x, p.y)) {
                selectedIndex = i;
                updateButtonSelection();
                anyHovered = true;
                break;
            }
        }
        if (!anyHovered) {
            selectedIndex = -1;
            updateButtonSelection();
        }
    }

    public void mouseClicked(Point p) {
        for (int i = 0; i < buttons.length; i++) {
            MenuButton btn = buttons[i];
            if (btn.contains(p.x, p.y)) {
                selectedIndex = i;
                updateButtonSelection();
                if (btn == okButton) {
                    tryStartGame();
                } else if (btn == backButton) {
                	game.getGameStateManager().setState(GameState.MAIN_MENU);
                }
            }
        }
    }

    private void tryStartGame() {
        String trimmedName = getTrimmedSaveName();
        if (trimmedName.isEmpty()) {
            errorMessage = "Name can't be empty.";
        } else {
            errorMessage = "";
            game.onStartGame();
        }
    }

    private void updateButtonSelection() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected(i == selectedIndex);
        }
    }

    public String getTrimmedSaveName() {
        return saveName.toString().trim();
    }

    public boolean isValidName() {
        return !getTrimmedSaveName().isEmpty();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }
}
