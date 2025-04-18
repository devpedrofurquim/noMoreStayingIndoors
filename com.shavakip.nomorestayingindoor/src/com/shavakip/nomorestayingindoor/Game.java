package com.shavakip.nomorestayingindoor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Game implements Runnable, KeyListener, MenuActionListener {

    private JFrame frame;
    private boolean running = false;
    public static final int WIDTH = 320;   // not used for rendering here but for title info etc.
    public static final int HEIGHT = 240;
    public static final String TITLE = "No More Staying Indoors";

    private Canvas canvas;
    private Player player;
    private MapBounds forestBounds;
    private float overlayAlpha = 0.0f;

    
    private final long CREDITS_TOTAL_DURATION = 10000; // 10 seconds total
    private final long CREDITS_FADE_DURATION = 1000;   // 1s fade in/out
    
    private CreditsScreen creditsScreen;

    
    private boolean showingCredits = false;
    private long creditsStartTime = 0;
    private final long CREDITS_DURATION = 5000; // 5 seconds
    
    private float fadeAlpha = 0.0f;  // Current alpha value (0.0 = fully transparent, 1.0 = fully opaque)
    private FadeState fadeState = FadeState.NONE;
    private long fadeDuration = 1000; // Duration of the fade effect in milliseconds (1000ms = 1 second)
    private long fadeStartTime = 0;   // The time when the fade effect starts
    
    private BitmapFont menuFont;
    
    private GameStateManager gameStateManager;
    
    private enum FadeState {
        NONE,
        FADE_OUT,
        FADE_IN
    }
    
    private FadeState previousFade = FadeState.NONE;

    private boolean upPressed, downPressed, leftPressed, rightPressed;

    private BufferedImage image;
    // Internal resolution: 300 width, and height computed as 300/16*9 (which is 162 in integer math)
    private final int INTERNAL_WIDTH = 300;
    private final int INTERNAL_HEIGHT = INTERNAL_WIDTH / 16 * 9; 
    private boolean fullscreen = true;
    private GraphicsDevice graphicsDevice;
    
    private MainMenu mainMenu;
    private PauseScreen pauseScreen;
    private GameOverScreen gameOverScreen;
    
    private float menuAlpha = 0.0f;
    private long menuFadeStartTime = 0;
    private final long MENU_FADE_DURATION = 1000; // 1 second in milliseconds
    
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
        
        menuFadeStartTime = System.currentTimeMillis();
        menuAlpha = 0.0f;

        frame.add(canvas);
        // Initially pack, though setFullscreen() will adjust things.
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Set fullscreen (or windowed) mod e.
        setFullscreen(fullscreen);

        frame.setVisible(true);

        player = new Player(new Position(100.0f, 100.0f));
        
        try {
            // Character order must match your image layout
        	menuFont = new BitmapFont("res/Kenney.ttf", 12f);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        mainMenu = new MainMenu(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        mainMenu.setMenuActionListener(this); // clearly set the listener!
        creditsScreen = new CreditsScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT, CREDITS_TOTAL_DURATION, CREDITS_FADE_DURATION);
        
        pauseScreen = new PauseScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        
        gameOverScreen = new GameOverScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        
        // Create forest bounds (for example, a 800x600 area)
        forestBounds = new MapBounds(0, 0, 512, 512);
        
        camera = new Camera(new Size(INTERNAL_WIDTH, INTERNAL_HEIGHT), forestBounds);
                
        camera.focusOn(player);

        gameStateManager = new GameStateManager(); // Start at MAIN_MENU
        
        canvas.addKeyListener(this);
        
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameStateManager.is(GameState.MAIN_MENU)) {
                    // Adjust coordinates for scaling/fullscreen clearly
                    Point p = translateMousePoint(e.getPoint());
                    mainMenu.mouseClicked(new MouseEvent(
                        e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), p.x, p.y, e.getClickCount(), e.isPopupTrigger()
                    ));
                }
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (gameStateManager.is(GameState.MAIN_MENU)) {
                    Point p = translateMousePoint(e.getPoint());
                    mainMenu.mouseMoved(new MouseEvent(
                        e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), p.x, p.y, e.getClickCount(), e.isPopupTrigger()
                    ));
                }
            }
        });
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
        if (fadeState != FadeState.NONE) {
            long currentTime = System.currentTimeMillis();
            float fadeProgress = (currentTime - fadeStartTime) / (float) fadeDuration;

            if (fadeProgress >= 1.0f) {
                fadeProgress = 1.0f;
                previousFade = fadeState;
                fadeState = FadeState.NONE;
            }

            if (fadeState == FadeState.FADE_IN) {
            	overlayAlpha = 1.0f - fadeProgress;
            } else if (fadeState == FadeState.FADE_OUT) {
            	overlayAlpha = fadeProgress;
            }
        }
        
        if (showingCredits) {
            long elapsed = System.currentTimeMillis() - creditsStartTime;

            // Wait 500ms before starting text visibility
            if (elapsed < 500) {
                fadeAlpha = 0f;
            } else if (elapsed > CREDITS_TOTAL_DURATION - CREDITS_FADE_DURATION) {
                float fadeOutTime = CREDITS_TOTAL_DURATION - elapsed;
                fadeAlpha = Math.max(0f, fadeOutTime / CREDITS_FADE_DURATION);
            } else {
                fadeAlpha = 1.0f;
            }

            // Exit credits after duration
            if (elapsed >= CREDITS_TOTAL_DURATION) {
                showingCredits = false;
                gameStateManager.setState(GameState.MAIN_MENU);
                mainMenu.resetFade();
                fadeAlpha = 0f;
            }

            return;
        }
        
        if (gameStateManager.is(GameState.MAIN_MENU)) {
            mainMenu.update();
        }

        if (gameStateManager.is(GameState.PLAYING)) {
            updatePlayerVelocity();
            player.update(deltaTime);

            // Clamp player to map bounds
            Position playerPos = player.getPosition();
            float clampedX = Math.max(forestBounds.getMinX(), Math.min(forestBounds.getMaxX() - 16, playerPos.getX()));
            float clampedY = Math.max(forestBounds.getMinY(), Math.min(forestBounds.getMaxY() - 16, playerPos.getY()));
            player.setPosition(new Position(clampedX, clampedY));

            camera.update();
        }
        
        if (gameStateManager.is(GameState.PAUSED)) {
            pauseScreen.update();
        }
        
        if (gameStateManager.is(GameState.GAME_OVER)) {
            gameOverScreen.update();
        }
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
        
        if (showingCredits) {
            long elapsed = System.currentTimeMillis() - creditsStartTime;
            creditsScreen.render(g, elapsed, fadeAlpha);
            g.dispose();
        }

        switch (gameStateManager.getState()) {
        case MAIN_MENU:
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

            mainMenu.render(g);
            break;
            case PLAYING:
                // Translate and scale camera
                Position camPos = camera.getPosition();
                g.scale(camera.getZoom(), camera.getZoom());
                g.translate(-camPos.getX(), -camPos.getY());

                // Draw grid lines
                int gridSize = 16;
                g.setColor(new Color(255, 255, 255, 40));

                int startX = (int) (Math.max(camPos.getX(), forestBounds.getMinX()) - (camPos.getX() % gridSize)) - gridSize;
                int endX = (int) (Math.min(camPos.getX() + INTERNAL_WIDTH, forestBounds.getMaxX())) + gridSize;
                int startU = (int) (Math.max(camPos.getY(), forestBounds.getMinY()) - (camPos.getY() % gridSize)) - gridSize;
                int endY = (int) (Math.min(camPos.getY() + INTERNAL_HEIGHT, forestBounds.getMaxY())) + gridSize;

                for (int x = startX; x <= endX; x += gridSize) {
                    g.drawLine(x, startU, x, endY);
                }

                for (int y = startU; y <= endY; y += gridSize) {
                    g.drawLine(startX, y, endX, y);
                }

                // Render player
                player.render(g);

                // Reset transform (optional)
                g.translate(camPos.getX(), camPos.getY());
                break;

            case PAUSED:
            	pauseScreen.render(g);
                break;

            case GAME_OVER:
                gameOverScreen.render(g);
                break;
        }

        g.dispose();

        // 2. Scale image for fullscreen output using full width & height
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
       

        // Draw fade overlay (if active)
        gFinal.setColor(new Color(0, 0, 0, overlayAlpha));
        gFinal.fillRect(0, 0, screenW, screenH);

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
        
        
        if (key == KeyEvent.VK_F11)
            setFullscreen(!fullscreen);

        // ===== MAIN MENU =====
        if (gameStateManager.is(GameState.MAIN_MENU)) {
            mainMenu.keyPressed(e);
            return;
        }

        // ===== GAME OVER =====
        if (gameStateManager.is(GameState.GAME_OVER)) {
            if (key == KeyEvent.VK_R) {
                gameStateManager.setState(GameState.MAIN_MENU); // Restart to menu
            }
            if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
            return;
        }

        // ===== PAUSED =====
        if (gameStateManager.is(GameState.PAUSED)) {
            if (key == KeyEvent.VK_P) {
                gameStateManager.setState(GameState.PLAYING);
            }
            if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
            return;
        }

        // ===== PLAYING =====
        if (gameStateManager.is(GameState.PLAYING)) {
        	  if (key == KeyEvent.VK_T) {
                  gameStateManager.setState(GameState.GAME_OVER);
                  gameOverScreen.resetFade();
              }

            if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
                upPressed = true;
            if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
                downPressed = true;
            if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
                leftPressed = true;
            if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
                rightPressed = true;

            if (key == KeyEvent.VK_P) {
                gameStateManager.setState(GameState.PAUSED); // Toggle pause
                pauseScreen.resetFade();
            }

            if (key == KeyEvent.VK_ESCAPE)
                System.exit(0);

            if (key == KeyEvent.VK_1) camera.shakeLight();
            if (key == KeyEvent.VK_2) camera.shakeDefault();
            if (key == KeyEvent.VK_3) camera.shakeHard();

            if (key == KeyEvent.VK_Z) camera.startZoomInEffect();
            if (key == KeyEvent.VK_X) camera.startZoomOutEffect();

            if (key == KeyEvent.VK_I) startFadeIn();
            if (key == KeyEvent.VK_O) startFadeOut();

            if (key == KeyEvent.VK_EQUALS || key == KeyEvent.VK_ADD)
                camera.zoomIn(0.1f);

            if (key == KeyEvent.VK_MINUS || key == KeyEvent.VK_SUBTRACT)
                camera.zoomOut(0.1f);
            
            if (key == KeyEvent.VK_R) {
                gameStateManager.setState(GameState.MAIN_MENU);
                mainMenu.resetFade();
            }
            
            updatePlayerVelocity();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (gameStateManager.is(GameState.MAIN_MENU)) {
            return; // If you have keyReleased logic in MainMenu, forward here too.
        }
        
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
    
    private void setFadeState(FadeState newState) {
        if (fadeState != newState) {
            System.out.println("FadeState changed: " + fadeState + " → " + newState);
            fadeState = newState;
        }
    }
    
    public void startFadeOut() {
        if (fadeState != FadeState.NONE) return; // Only allow if not already fading
        if (previousFade == FadeState.FADE_OUT) return;
        setFadeState(FadeState.FADE_OUT);
        fadeAlpha = 0.0f;
        fadeState = FadeState.FADE_OUT;
        fadeStartTime = System.currentTimeMillis();
    }

    public void startFadeIn() {
        if (fadeState != FadeState.NONE) return;           // Only allow if not already fading
        if (previousFade != FadeState.FADE_OUT) return;    // Only allow if last fade was fadeOut
        setFadeState(FadeState.FADE_IN);
        fadeAlpha = 1.0f;
        fadeState = FadeState.FADE_IN;
        fadeStartTime = System.currentTimeMillis();
    }
    
    private void drawMenuButton(Graphics2D g, int x, int y, int width, int height, String label) {
        // Button background
        g.setColor(Color.LIGHT_GRAY);
        g.fillRoundRect(x, y, width, height, 10, 10);

        // Button border
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, width, height, 10, 10);

        // Label
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getAscent();
        int textX = x + (width - textWidth) / 2;
        int textY = y + ((height + textHeight) / 2) - 3;
        g.drawString(label, textX, textY);
    }
    
    private Point translateMousePoint(Point p) {
        int screenW = canvas.getWidth();
        int screenH = canvas.getHeight();

        double scaleX = screenW / (double) INTERNAL_WIDTH;
        double scaleY = screenH / (double) INTERNAL_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        int finalW = (int) (INTERNAL_WIDTH * scale);
        int finalH = (int) (INTERNAL_HEIGHT * scale);

        int xOffset = (screenW - finalW) / 2;
        int yOffset = (screenH - finalH) / 2;

        int translatedX = (int)((p.x - xOffset) / scale);
        int translatedY = (int)((p.y - yOffset) / scale);

        return new Point(translatedX, translatedY);
    }
    
    @Override
    public void onStartGame() {
        gameStateManager.setState(GameState.PLAYING);
        System.out.println("Game Started!");
    }

    @Override
    public void onOptions() {
        System.out.println("Options clicked! (not implemented yet)");
    }

    @Override
    public void onExit() {
        System.exit(0);
    }

	@Override
	public void onNewGame() {
	    System.out.println("Naming your save... (not implemented)");
	}

	@Override
	public void onLoadGame() {
	    System.out.println("Showing save slots... (not implemented)");		
	}

	@Override
	public void onBack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCredits() {
	    showingCredits = true;
	    creditsStartTime = System.currentTimeMillis();
	    fadeAlpha = 0f;
	    fadeStartTime = System.currentTimeMillis();
	}

	@Override
	public void onSecretMessage() {
		// TODO Auto-generated method stub
		
	}
}
