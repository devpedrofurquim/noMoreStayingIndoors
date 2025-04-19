package com.shavakip.nomorestayingindoor;

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

    public void deleteSlot(int slot) {
        File f = getSaveFile(slot);
        if (f.exists()) f.delete();
    }

    private File getSaveFile(int slot) {
        return new File(SAVE_FOLDER + "/save" + slot + ".dat");
    }
}
