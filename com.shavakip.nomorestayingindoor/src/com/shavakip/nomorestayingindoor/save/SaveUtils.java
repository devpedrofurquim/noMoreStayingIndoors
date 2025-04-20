package com.shavakip.nomorestayingindoor.save;

public class SaveUtils {
    private static final int TOTAL_STORY_POINTS = 100; // Change as your game grows

    /**
     * Calculates the percentage of story completion.
     *
     * @param progress the current story progress
     * @return percentage from 0 to 100
     */
    public static int calculateProgressPercentage(int progress) {
        return Math.min(100, (int)((progress / (float) TOTAL_STORY_POINTS) * 100));
    }
}
