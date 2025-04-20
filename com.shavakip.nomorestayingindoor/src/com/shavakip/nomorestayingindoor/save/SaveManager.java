package com.shavakip.nomorestayingindoor.save;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FOLDER = "saves";

    public SaveManager() {
        File folder = new File(SAVE_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public void saveToSlot(int slot, SaveData data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getSaveFile(slot)))) {
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SaveData loadFromSlot(int slot) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(getSaveFile(slot)))) {
            return (SaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public boolean hasSave(int slot) {
        return getSaveFile(slot).exists();
    }

    public void deleteSaveSlot(int slot) {
        File file = getSaveFile(slot);
        if (file.exists()) {
            file.delete();
            System.out.println("Deleted file: " + file.getAbsolutePath());
        } else {
            System.out.println("File not found: " + file.getAbsolutePath());
        }
    }
    
    /**
     * Loads the existing save from the given slot, updates a specific story choice,
     * player position, story progress, and save name, then writes the updated data back to disk.
     *
     * @param slot           The save slot number (1-based index).
     * @param key            The key identifying a story-related choice or flag.
     * @param value          The value associated with the choice (e.g., "true", "optionA").
     * @param storyProgress  The current story progress (e.g., chapter or scene number).
     * @param x              The player's X coordinate at the time of save.
     * @param y              The player's Y coordinate at the time of save.
     * @param saveName       The updated display name for this save slot.
     */
    public void updateSaveSlot(int slot, String key, String value, int storyProgress, float x, float y, String saveName) {
        SaveData data = loadFromSlot(slot);
        if (data == null) return;

        data.setChoice(key, value);
        data.storyProgress = storyProgress;
        data.playerX = x;
        data.playerY = y;
        data.saveName = saveName;

        saveToSlot(slot, data);
    }

    private File getSaveFile(int slot) {
        return new File(SAVE_FOLDER + "/save" + slot + ".dat");
    }
}
