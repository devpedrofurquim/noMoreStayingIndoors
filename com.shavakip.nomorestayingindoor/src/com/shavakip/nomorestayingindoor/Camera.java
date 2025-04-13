package com.shavakip.nomorestayingindoor;

import java.util.Optional;

public class Camera {
    private Position position;
    private Size windowSize;
    private Optional<GameObject> objectWithFocus = Optional.empty();
    private MapBounds mapBounds;
    
    private float followSmoothness = 0.1f; // Lower = slower, higher = snappier (range: 0 < smoothness ≤ 1)
    
    private boolean justFocused = true;
    
    private float zoom = 1.0f; // default = 100%
    private float shakeIntensity = 0f;
    private long shakeDuration = 0;
    private long shakeStartTime = 0;
    
    private boolean zoomingIn = false;
    private float zoomSpeed = 0.01f; // how fast it zooms in per frame/tick
    
    private boolean zoomingOut = false;
    private float targetZoom = 1.0f;


    private float shakeOffsetX = 0f;
    private float shakeOffsetY = 0f;
    
    private static final float MIN_ZOOM = 0.5f;  // zoomed out (50%)
    private static final float MAX_ZOOM = 2.0f;  // zoomed in (200%)
    private static final float DEF_ZOOM = 1.0f;  // zoomed in (200%)
    
 // Preset shake types
    public static final float SHAKE_LIGHT_INTENSITY = 1.5f;
    public static final float SHAKE_DEFAULT_INTENSITY = 4.0f;
    public static final float SHAKE_HARD_INTENSITY = 8.0f;

    public static final long SHAKE_LIGHT_DURATION = 150;
    public static final long SHAKE_DEFAULT_DURATION = 250;
    public static final long SHAKE_HARD_DURATION = 400;


    public Camera(Size windowSize, MapBounds mapBounds) {
        this.position = new Position(0, 0);
        this.windowSize = windowSize;
        this.mapBounds = mapBounds;
    }

    public void focusOn(GameObject obj) {
        this.objectWithFocus = Optional.of(obj);
        this.justFocused = true;
    }

    public void update() {
        if (objectWithFocus.isPresent()) {
            Position objectPosition = objectWithFocus.get().getPosition();

            float centerX = objectPosition.getX() + 8;
            float centerY = objectPosition.getY() + 8;

            float camX = centerX - windowSize.getWidth() / (2f * zoom);
            float camY = centerY - windowSize.getHeight() / (2f * zoom);

            float maxCamX = mapBounds.getMaxX() - windowSize.getWidth() / zoom;
            float maxCamY = mapBounds.getMaxY() - windowSize.getHeight() / zoom;

            camX = Math.max(mapBounds.getMinX(), Math.min(camX, maxCamX));
            camY = Math.max(mapBounds.getMinY(), Math.min(camY, maxCamY));
            
            if (zoomingIn && zoom < MAX_ZOOM) {
                setZoom(zoom + zoomSpeed);
                if (zoom >= MAX_ZOOM) {
                    zoom = MAX_ZOOM;
                    zoomingIn = false;
                }
            }
            
            if (zoomingOut && zoom > DEF_ZOOM) {
                setZoom(zoom - zoomSpeed);
                if (zoom <= DEF_ZOOM) {
                    zoom = DEF_ZOOM;
                    zoomingOut = false;
                }
            }

            float targetX = camX;
            float targetY = camY;

            if (justFocused) {
                position.setX(targetX);
                position.setY(targetY);
                justFocused = false;
            } else {
                float newX = lerp(position.getX(), targetX, followSmoothness);
                float newY = lerp(position.getY(), targetY, followSmoothness);
                position.setX(newX);
                position.setY(newY);
            }

            // Now apply shake *after* smoothing
            position.setX(position.getX() + shakeOffsetX);
            position.setY(position.getY() + shakeOffsetY);
            
         // Apply shake with decay
            long now = System.currentTimeMillis();
            if (now - shakeStartTime < shakeDuration) {
                float timeRatio = 1f - (float)(now - shakeStartTime) / shakeDuration;
                float decay = shakeIntensity * timeRatio;
                shakeOffsetX = (float) ((Math.random() - 0.5f) * 2 * decay);
                shakeOffsetY = (float) ((Math.random() - 0.5f) * 2 * decay);
            } else {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }
        }
    }
    
    public void setZoom(float zoom) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(zoom, MAX_ZOOM));
    }
    
    public void startZoomInEffect() {
        startZoomInEffect(.05f); // default speed
    }
    
    public void startZoomOutEffect(float speed) {
        this.zoomingOut = true;
        this.zoomSpeed = speed;
        this.targetZoom = DEF_ZOOM;
    }
    
    public void startZoomOutEffect() {
        startZoomOutEffect(0.05f); // default zoom-out speed
    }
    
    public void resetZoom() {
        setZoom(DEF_ZOOM);
        zoomingIn = false;
        zoomingOut = false;
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public void startZoomInEffect(float speed) {
        this.zoomingIn = true;
        this.zoomSpeed = speed;
    }
    
    public void zoomIn(float step) {
        setZoom(zoom + step);
    }

    public void zoomOut(float step) {
        setZoom(zoom - step);
    }
    
    public void shakeLight() {
        shake(SHAKE_LIGHT_INTENSITY, SHAKE_LIGHT_DURATION);
    }

    public void shakeDefault() {
        shake(SHAKE_DEFAULT_INTENSITY, SHAKE_DEFAULT_DURATION);
    }

    public void shakeHard() {
        shake(SHAKE_HARD_INTENSITY, SHAKE_HARD_DURATION);
    }
    
    public void shake(float intensity, long durationMillis) {
        this.shakeIntensity = intensity;
        this.shakeDuration = durationMillis;
        this.shakeStartTime = System.currentTimeMillis();
    }

    public Position getPosition() {
        return position;
    }
    
    private float lerp(float start, float end, float amt) {
        return start + (end - start) * amt;
    }
}
