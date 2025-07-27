package com.shavakip.nomorestayingindoor.world;

import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;



public class GameMap {
    private String id;
    private MapBounds bounds;
    private List<Door> doors;

    public GameMap(String id, MapBounds bounds) {
        this.id = id;
        this.bounds = bounds;
        this.doors = new ArrayList<>();
    }

    public String getId() { return id; }
    public MapBounds getBounds() { return bounds; }
    public List<Door> getDoors() { return doors; }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public void render(Graphics2D g) {
        // Set a unique color for each map based on its ID
        switch (id) {
            case "forest":
                g.setColor(new Color(34, 139, 34)); // forest green
                break;
            case "lake":
                g.setColor(new Color(70, 130, 180)); // lake blue
                break;
            case "cemetery":
                g.setColor(new Color(90, 90, 90)); // cemetery gray
                break;
            default:
                g.setColor(Color.PINK); // unknown maps
                break;
        }

        // Fill background
        g.fillRect(
            (int) bounds.getMinX(),
            (int) bounds.getMinY(),
            (int)(bounds.getMaxX() - bounds.getMinX()),
            (int)(bounds.getMaxY() - bounds.getMinY())
        );

        // Render door rectangles
        for (Door door : doors)
            door.render(g);

        // Optional: Debug overlay
        g.setColor(Color.WHITE);
        g.drawString("Map: " + id, (int) bounds.getMinX() + 10, (int) bounds.getMinY() + 20);
    }
}
