package com.shavakip.nomorestayingindoor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Game implements Runnable, KeyListener {

    private JFrame frame;
    private boolean running = false;
    public static final int WIDTH = 320;   // not used for rendering here but for title info etc.
    public static final int HEIGHT = 240;
    public static final String TITLE = "No More Staying Indoors";

    private Canvas canvas;
    private Player player;
    private MapBounds forestBounds;


    private boolean upPressed, downPressed, leftPressed, rightPressed;

    private BufferedImage image;
    // Internal resolution: 300 width, and height computed as 300/16*9 (which is 162 in integer math)
    private final int INTERNAL_WIDTH = 300;
    private final int INTERNAL_HEIGHT = INTERNAL_WIDTH / 16 * 9; 
    private boolean fullscreen = true;
    private GraphicsDevice graphicsDevice;
    
    private Camera camera;

    private volatile boolean safeToRender = true;

    public Game() {
        // Create the internal image (game buffer).
        image = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Get system graphics device (for fullscreen mode) and screen size.
        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Create canvas.
        // Initially, we set its preferred size to our internal resolution. 
        // In fullscreen mode we will override that.
        canvas = new Canvas() {
            @Override
            public Dimension getPreferredSize() {
                return getSize();
            }
        };
        canvas.setFocusable(true);
        canvas.setIgnoreRepaint(true);

        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true); // no window borders
        frame.setResizable(true);
        // testasd

        frame.add(canvas);
        // Initially pack, though setFullscreen() will adjust things.
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Set fullscreen (or windowed) mode.
        setFullscreen(fullscreen);

        frame.setVisible(true);

        player = new Player(new Position(100.0f, 100.0f));
        
        // Create forest bounds (for example, a 800x600 area)
        forestBounds = new MapBounds(0, 0, 512, 512);
        
        camera = new Camera(new Size(INTERNAL_WIDTH, INTERNAL_HEIGHT), forestBounds);
        camera.focusOn(player);

        canvas.addKeyListener(this);
    }

    public synchronized void start() {
        running = true;
        new Thread(this).start();
    }

    public synchronized void stop() {
        running = false;
    }

    /**
     * Adjusts the frame and canvas for fullscreen or windowed mode.
     * In fullscreen mode, we force the canvas to fill the entire screen.
     * In windowed mode, we use a scaling factor.
     */
    private void setFullscreen(boolean enable) {
        safeToRender = false;
        fullscreen = enable;

        // Dispose frame before changes
        frame.dispose();

        // Remove current Canvas from frame (important!)
        frame.remove(canvas);

        // Toggle undecorated mode
        frame.setUndecorated(fullscreen);

        // Set fullscreen mode or windowed mode
        if (fullscreen) {
        	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        	frame.setSize(screenSize.width, screenSize.height);
        	frame.setLocation(0, 0);
        } else {
        	frame.setSize(INTERNAL_WIDTH * 4, INTERNAL_HEIGHT * 4);
            frame.setLocationRelativeTo(null);
        }

        // Add a fresh Canvas (this)
        frame.add(canvas);
        frame.setVisible(true);

        // Delay and setup
        SwingUtilities.invokeLater(() -> {
        	canvas.createBufferStrategy(3);
        	canvas.requestFocusInWindow(); // Ensures key input still works
            safeToRender = true;
        });
    }


    @Override
    public void run() {
        canvas.requestFocusInWindow();
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / 60.0; // 60 ticks per second
        double delta = 0;

        int frames = 0;
        int updates = 0;
        long timer = System.currentTimeMillis();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                double deltaTime = nsPerTick / 1_000_000_000.0;
                tick(deltaTime);
                updates++;
                delta--;
            }

            render();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + " | Updates: " + updates);
                frames = 0;
                updates = 0;
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stop();
    }

    public void tick(double deltaTime) {
        updatePlayerVelocity(); // ← update velocity every tick

        // 1. Move player
        player.update(deltaTime);

        // 2. Clamp position AFTER movement, accounting for player size (16x16)
        Position playerPos = player.getPosition();
        float clampedX = Math.max(forestBounds.getMinX(), Math.min(forestBounds.getMaxX() - 16, playerPos.getX()));
        float clampedY = Math.max(forestBounds.getMinY(), Math.min(forestBounds.getMaxY() - 16, playerPos.getY()));
        player.setPosition(new Position(clampedX, clampedY));
        
        System.out.printf("Player: (%.1f, %.1f)%n", player.getPosition().x, player.getPosition().y);


        // 3. Update camera
        camera.update();
    }

    /**
     * Render method:
     * 1. Renders the complete game world into the internal image (300×162).
     * 2. Crops a square region from the internal image using the full height (i.e. 162×162).
     * 3. Scales the cropped square so that its height exactly matches the canvas height.
     * 4. Draws the scaled square centered horizontally so that black bars appear on the left and right.
     */
    public void render() {
        if (!safeToRender) return;

        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }

        // 1. Render the game world into the internal buffer.
        Graphics2D g = image.createGraphics();
        g.setColor(Color.PINK);
        g.fillRect(0, 0, INTERNAL_WIDTH, INTERNAL_HEIGHT);

        // 2. Translate the graphics context to simulate camera movement
        Position camPos = camera.getPosition();
        g.translate(-camPos.getX(), -camPos.getY());

        // Draw grid lines
        int gridSize = 16;
        g.setColor(new Color(255, 255, 255, 40));

        int startX = (int) (Math.max(camPos.getX(), forestBounds.getMinX()) - (camPos.getX() % gridSize)) - gridSize;
        int endX = (int) (Math.min(camPos.getX() + INTERNAL_WIDTH, forestBounds.getMaxX())) + gridSize;
        int startY = (int) (Math.max(camPos.getY(), forestBounds.getMinY()) - (camPos.getY() % gridSize)) - gridSize;
        int endY = (int) (Math.min(camPos.getY() + INTERNAL_HEIGHT, forestBounds.getMaxY())) + gridSize;

        for (int x = startX; x <= endX; x += gridSize) {
            g.drawLine(x, startY, x, endY);
        }

        for (int y = startY; y <= endY; y += gridSize) {
            g.drawLine(startX, y, endX, y);
        }

        // Render player
        player.render(g);

        // Reset translation (optional)
        g.translate(camPos.getX(), camPos.getY());
        g.dispose();

        // 3. Scale image for fullscreen output using full width & height
        int screenW = canvas.getWidth();
        int screenH = canvas.getHeight();

        int imageW = INTERNAL_WIDTH;
        int imageH = INTERNAL_HEIGHT;

        double scaleX = screenW / (double) imageW;
        double scaleY = screenH / (double) imageH;
        double scale = Math.min(scaleX, scaleY); // keep aspect ratio

        int finalW = (int) (imageW * scale);
        int finalH = (int) (imageH * scale);

        int xOffset = (screenW - finalW) / 2;
        int yOffset = (screenH - finalH) / 2;

        Graphics2D gFinal = (Graphics2D) bs.getDrawGraphics();
        gFinal.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gFinal.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        gFinal.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        gFinal.setColor(Color.BLACK);
        gFinal.fillRect(0, 0, screenW, screenH);

        gFinal.drawImage(image,
                xOffset, yOffset, xOffset + finalW, yOffset + finalH,
                0, 0, imageW, imageH,
                null);

        gFinal.dispose();
        bs.show();
    }




    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }

    private void updatePlayerVelocity() {
        int vx = 0, vy = 0;
        if (upPressed) vy -= 2;
        if (downPressed) vy += 2;
        if (leftPressed) vx -= 2;
        if (rightPressed) vx += 2;
        player.setVelocity(vx, vy);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
            upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            rightPressed = true;
        if (key == KeyEvent.VK_F11)
            setFullscreen(!fullscreen);
        if (key == KeyEvent.VK_ESCAPE)
            System.exit(0);
        updatePlayerVelocity();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
            upPressed = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            downPressed = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            leftPressed = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            rightPressed = false;
        updatePlayerVelocity();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }
}
