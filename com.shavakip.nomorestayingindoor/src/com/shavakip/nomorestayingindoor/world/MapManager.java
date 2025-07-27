package com.shavakip.nomorestayingindoor.world;

import java.util.HashMap;
import java.util.Map;

public class MapManager {
    private Map<String, GameMap> maps = new HashMap<>();
    private GameMap currentMap;

    public void addMap(GameMap map) {
        maps.put(map.getId(), map);
    }

    public void setCurrentMap(String id) {
        currentMap = maps.get(id);
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }
}