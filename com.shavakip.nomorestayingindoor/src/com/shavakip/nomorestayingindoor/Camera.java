package com.shavakip.nomorestayingindoor;

import java.util.Optional;

public class Camera {
    private Position position;
    private Size windowSize;
    private Optional<GameObject> objectWithFocus = Optional.empty();
    private MapBounds mapBounds;

    public Camera(Size windowSize, MapBounds mapBounds) {
        this.position = new Position(0, 0);
        this.windowSize = windowSize;
        this.mapBounds = mapBounds;
    }

    public void focusOn(GameObject obj) {
        this.objectWithFocus = Optional.of(obj);
    }

    public void update() {
        if (objectWithFocus.isPresent()) {
            Position objectPosition = objectWithFocus.get().getPosition();

            float centerX = objectPosition.getX() + 8;  // Player center
            float centerY = objectPosition.getY() + 8;

            float camX = centerX - windowSize.getWidth() / 2f;
            float camY = centerY - windowSize.getHeight() / 2f;

            float maxCamX = mapBounds.getMaxX() - windowSize.getWidth();
            float maxCamY = mapBounds.getMaxY() - windowSize.getHeight();

            // Clamp the camera to the map
            camX = Math.max(mapBounds.getMinX(), Math.min(camX, maxCamX));
            camY = Math.max(mapBounds.getMinY(), Math.min(camY, maxCamY));

            position.setX(camX);
            position.setY(camY);
        }

        System.out.printf("Camera: (%.2f, %.2f)%n", position.getX(), position.getY());
    }

    public Position getPosition() {
        return position;
    }
}
