package com.shavakip.nomorestayingindoor.core;

public class GameStateManager {

    private GameState currentState;

    public GameStateManager() {
        currentState = GameState.MAIN_MENU; // Start at the main menu
    }

    public GameState getState() {
        return currentState;
    }

    public void setState(GameState newState) {
        currentState = newState;
    }

    public boolean is(GameState state) {
        return currentState == state;
    }
}
