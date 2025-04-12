package com.shavakip.nomorestayingindoor;

import java.util.Optional;

public class Camera {
    private Position position;
    private Size windowSize;
    private Optional<GameObject> objectWithFocus = Optional.empty();
    
    private float smoothingFactor = .1f; // Value between 0 and 1 (lower = slower)
    private boolean isCentered = false;  // Flag to track if the camera is centered initially

    public Camera(Size windowSize) {
        this.position = new Position(0, 0);
        this.windowSize = windowSize;
    }

    public void focusOn(GameObject obj) {
        this.objectWithFocus = Optional.of(obj);
    }

    public void update() {
        if (objectWithFocus.isPresent()) {
            Position objectPosition = objectWithFocus.get().getPosition();

            // Center camera at the start
            if (!isCentered) {
                position.setX(objectPosition.getX() - windowSize.getWidth() / 2);
                position.setY(objectPosition.getY() - windowSize.getHeight() / 2);
                isCentered = true;  // Mark as centered after the first update
            } else {
                // Smooth movement for subsequent updates
                float targetX = objectPosition.getX() - windowSize.getWidth() / 2;
                float targetY = objectPosition.getY() - windowSize.getHeight() / 2;

                // Interpolate the camera's current position towards the target position
                position.setX(lerp(position.getX(), targetX, smoothingFactor));
                position.setY(lerp(position.getY(), targetY, smoothingFactor));
            }
        }
    }

    // Linear interpolation (lerp) method to smoothly move from current value to target value
    private float lerp(float current, float target, float alpha) {
        return current + (target - current) * alpha;
    }

    public Position getPosition() {
        return position;
    }
}
