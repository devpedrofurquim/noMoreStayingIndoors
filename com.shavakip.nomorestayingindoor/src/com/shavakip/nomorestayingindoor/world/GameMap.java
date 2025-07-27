package com.shavakip.nomorestayingindoor.world;

import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import com.shavakip.nomorestayingindoor.entity.GameObject; // important import!

public class GameMap {
    private String id;
    private MapBounds bounds;
    private List<Door> doors;
    private TileMap tileMap;
    private List<GameObject> objects; // ✅ add this line

    public GameMap(String id, MapBounds bounds) {
        this.id = id;
        this.bounds = bounds;
        this.doors = new ArrayList<>();
        this.objects = new ArrayList<>(); // ✅ initialize here
    }

    public String getId() { return id; }
    public MapBounds getBounds() { return bounds; }
    public List<Door> getDoors() { return doors; }
    public List<GameObject> getObjects() { return objects; } // ✅ getter for objects

    public void addDoor(Door door) {
        doors.add(door);
    }
    
    public void setTileMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }
    
    public TileMap getTileMap() {
        return tileMap;
    }
    
    public void addObject(GameObject obj) { // ✅ add this method
        objects.add(obj);
    }

    public void render(Graphics2D g, Camera camera) {
        if (tileMap != null) {
            tileMap.render(g, bounds.getMinX(), bounds.getMinY());
        } else {
            g.setColor(Color.GRAY);
            g.fillRect((int) bounds.getMinX(), (int) bounds.getMinY(),
                       (int)(bounds.getMaxX() - bounds.getMinX()),
                       (int)(bounds.getMaxY() - bounds.getMinY()));
        }

        // ✅ render all GameObjects:
        for (GameObject obj : objects) {
            obj.render(g, camera);
        }

        // doors last (optional):
        for (Door door : doors)
            door.render(g);
    }
}
