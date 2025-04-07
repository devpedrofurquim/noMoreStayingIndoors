package com.shavakip.nomorestayingindoor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Game extends Canvas implements Runnable, KeyListener  {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
    private boolean running = false;
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    public static final String TITLE = "No More Staying Indoors";
    
    private Player player;
    
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    
    private BufferedImage image;
    private final int INTERNAL_WIDTH = 960;
    private final int INTERNAL_HEIGHT = 540;
    private final int SCALE = 3; // Or use full screen scale ratio

    public Game() {
 
    	image = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        // Get screen dimensions
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = gd.getDisplayMode();
        int screenWidth = displayMode.getWidth();
        int screenHeight = displayMode.getHeight();
        
        
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true); // Removes window borders
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        
        gd.setFullScreenWindow(frame);
        
        frame.setVisible(true);

        
        player = new Player(100, 100); // starting position
        
        addKeyListener(this);
    }

    public synchronized void start() {
        running = true;
        new Thread(this).start();
    }

    public synchronized void stop() {
        running = false;
    }

    public void run() {
        requestFocus();
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / 60.0;
        double delta = 0;

        int frames = 0;
        int updates = 0;
        long timer = System.currentTimeMillis();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
        	  double deltaTime = nsPerTick / 1_000_000_000.0; // delta time per tick (in seconds)
    	    	tick(deltaTime); // ✅ new
        	    updates++;
        	    delta--;
            }

            render();
            frames++;

            // Print updates and FPS every second
            if (System.currentTimeMillis() - timer >= 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + " | Updates: " + updates);
                frames = 0;
                updates = 0;
            }
            
            // Add a small sleep to reduce CPU usage and cap FPS
            try {
                Thread.sleep(2); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stop();
    }

    public void tick(double deltaTime) {
        player.update(deltaTime);
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        // Render to the off-screen internal resolution
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        player.render(g);
        g.dispose();

        // Get actual screen size from the display mode
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        int screenW = dm.getWidth();
        int screenH = dm.getHeight();

        // Maintain aspect ratio
        double scaleX = screenW / (double) INTERNAL_WIDTH;
        double scaleY = screenH / (double) INTERNAL_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        int finalW = (int)(INTERNAL_WIDTH * scale);
        int finalH = (int)(INTERNAL_HEIGHT * scale);
        int xOffset = (screenW - finalW) / 2;
        int yOffset = (screenH - finalH) / 2;

        // Now draw the scaled image to screen
        Graphics gFinal = bs.getDrawGraphics();
        gFinal.setColor(Color.BLACK);
        gFinal.fillRect(0, 0, screenW, screenH); // Clear background
        gFinal.drawImage(image, xOffset, yOffset, finalW, finalH, null);
        gFinal.dispose();
        bs.show();
    }
    
    
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
    
    private void updatePlayerVelocity() {
        int vx = 0;
        int vy = 0;

        if (upPressed) vy -= 2;
        if (downPressed) vy += 2;
        if (leftPressed) vx -= 2;
        if (rightPressed) vx += 2;

        player.setVelocity(vx, vy);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = true;

        updatePlayerVelocity();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) downPressed = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = false;

        updatePlayerVelocity();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}
