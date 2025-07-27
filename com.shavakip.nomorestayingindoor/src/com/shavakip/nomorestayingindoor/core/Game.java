package com.shavakip.nomorestayingindoor.core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.shavakip.nomorestayingindoor.entity.Flower;
import com.shavakip.nomorestayingindoor.entity.Npc;
import com.shavakip.nomorestayingindoor.entity.Player;
import com.shavakip.nomorestayingindoor.entity.Tree;
import com.shavakip.nomorestayingindoor.graphics.BitmapFont;
import com.shavakip.nomorestayingindoor.save.SaveData;
import com.shavakip.nomorestayingindoor.save.SaveManager;
import com.shavakip.nomorestayingindoor.save.SaveUtils;
import com.shavakip.nomorestayingindoor.ui.CreditsScreen;
import com.shavakip.nomorestayingindoor.ui.GameOverScreen;
import com.shavakip.nomorestayingindoor.ui.IntroScreen;
import com.shavakip.nomorestayingindoor.ui.LoadGameScreen;
import com.shavakip.nomorestayingindoor.ui.MainMenu;
import com.shavakip.nomorestayingindoor.ui.NewGameScreen;
import com.shavakip.nomorestayingindoor.ui.PauseScreen;
import com.shavakip.nomorestayingindoor.ui.SavePopup;
import com.shavakip.nomorestayingindoor.world.Camera;
import com.shavakip.nomorestayingindoor.world.Door;
import com.shavakip.nomorestayingindoor.world.GameMap;
import com.shavakip.nomorestayingindoor.world.MapBounds;
import com.shavakip.nomorestayingindoor.world.MapManager;
import com.shavakip.nomorestayingindoor.world.Position;
import com.shavakip.nomorestayingindoor.world.TileMap;

public class Game implements Runnable, KeyListener, MenuActionListener {

    private JFrame frame;
    private boolean running = false;
    public static final int WIDTH = 320;   // not used for rendering here but for title info etc.
    public static final int HEIGHT = 240;
    public static final String TITLE = "No More Staying Indoors";
    
    private IntroScreen introScreen;

    private Canvas canvas;
    private Player player;
    private boolean justTeleported = false;
    private int teleportCooldown = 0;
    private MapBounds forestBounds;
    private float overlayAlpha = 0.0f;
    private String pendingSaveName = null;
    
    private int currentSaveSlot = -1; // -1 means no save loaded
    
    public SaveManager saveManager;
    
    private final long CREDITS_TOTAL_DURATION = 10000; // 10 seconds total
    private final long CREDITS_FADE_DURATION = 1000;   // 1s fade in/out
    
    private CreditsScreen creditsScreen;
    
    private MapManager mapManager;

    private boolean showingCredits = false;
    private long creditsStartTime = 0;
    private final long CREDITS_DURATION = 5000; // 5 seconds
    
    private float fadeAlpha = 0.0f;  // Current alpha value (0.0 = fully transparent, 1.0 = fully opaque)
    private FadeState fadeState = FadeState.NONE;
    private long fadeDuration = 1000; // Duration of the fade effect in milliseconds (1000ms = 1 second)
    private long fadeStartTime = 0;   // The time when the fade effect starts
    
    private String saveMessage = null;
    private long saveMessageStartTime = 0;
    private final long SAVE_MESSAGE_DURATION = 2500; // 2.5 seconds
    
    private BitmapFont menuFont;
    
    private GameStateManager gameStateManager;
    
    private SavePopup savePopup;
    
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
    
    private NewGameScreen newGameScreen;
    private LoadGameScreen loadGameScreen;
    
    private Camera camera;
    
    private int storyProgress = 78;

    private volatile boolean safeToRender = true;

    public Game() {
        // Create the internal image (game buffer).
        image = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Get system graphics device (for fullscreen mode) and screen size.
        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Map Manager
        mapManager = new MapManager();

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
        
     // FOREST
        String[] forestLayout = {
        	    "########################################",
        	    "#..t.....n....t...........t....t.......#",
        	    "#..T........t.T...........T....T.......#",
        	    "#..r........T.T......t....T....T.......#",
        	    "#...........t.t......T.........t.......#",
        	    "#...........T.T................T.......#",
        	    "#.............t......t.............n....#",
        	    "#.............T......T..................#",
        	    "#.....t.................................#",
        	    "#.....T...............r.................#",
        	    "#...........t........t......t...........#",
        	    "#...........T........T......T...........#",
        	    "#......t....t...............t...........#",
        	    "#......T....T...............T...........#",
        	    "#......t......t....t..........t.........#",
        	    "#......T......T....T..........T.........#",
        	    "#.............t....t..........t.........#",
        	    "#.............T....T..........T.........#",
        	    "#.............t.....................L...#",
        	    "#.............T.........................#",
        	    "########################################"
        	};
        
        int forestWidth = forestLayout[0].length() * TileMap.TILE_SIZE;
        int forestHeight = forestLayout.length * TileMap.TILE_SIZE;
        GameMap forest = new GameMap("forest", new MapBounds(0, 0, forestWidth, forestHeight));

        TileMap forestTileMap = new TileMap(forestLayout);
        forest.setTileMap(forestTileMap);
        populateObjectsFromTileMap(forest, forestTileMap);

        // LAKE
        String[] lakeLayout = {
        	    "########################################",
        	    "#..........~~~~~~~.....................#",
        	    "#..F......~~~~~~~~~..................C.#",
        	    "#..........~~~~~~~~~...................#",
        	    "#.............~~~~~....................#",
        	    "#.........................~............#",
        	    "#.....................~~~~~~~..........#",
        	    "#.............~~~~~....................#",
        	    "#..........~~~~~~~~~...................#",
        	    "#..........~~~~~~~~~...................#",
        	    "#..........~~~~~~~.....................#",
        	    "#......................................#",
        	    "#......................................#",
        	    "########################################"
        	};


		int lakeWidth = lakeLayout[0].length() * TileMap.TILE_SIZE;
		int lakeHeight = lakeLayout.length * TileMap.TILE_SIZE;
		
		GameMap lake = new GameMap("lake", new MapBounds(0, 0, lakeWidth, lakeHeight));
        TileMap lakeTileMap = new TileMap(lakeLayout);
        lake.setTileMap(lakeTileMap);
        populateObjectsFromTileMap(lake, lakeTileMap);

        // CEMETERY
        String[] cemeteryLayout = {
        	    "########################################",
        	    "#L........††††††.......................#",
        	    "#..†††..........†††....................#",
        	    "#.....†††..................†††.........#",
        	    "#...............††††...................#",
        	    "#....................††................#",
        	    "#.....††††††.................†††††.....#",
        	    "#....................†††...............#",
        	    "#..†††..........†††....................#",
        	    "#...............†††††..................#",
        	    "#...............††††...................#",
        	    "#......................................#",
        	    "########################################"
        	};
        int cemeteryWidth = cemeteryLayout[0].length() * TileMap.TILE_SIZE;
        int cemeteryHeight = cemeteryLayout.length * TileMap.TILE_SIZE;

        GameMap cemetery = new GameMap("cemetery", new MapBounds(0, 0, cemeteryWidth, cemeteryHeight));
        TileMap cemeteryTileMap = new TileMap(cemeteryLayout);
        cemetery.setTileMap(cemeteryTileMap);
        populateObjectsFromTileMap(cemetery, cemeteryTileMap);

        // Register all maps
        mapManager.addMap(forest);
        mapManager.addMap(lake);
        mapManager.addMap(cemetery);

        // Set the starting map
        mapManager.setCurrentMap("forest");

        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true); // no window borders
        frame.setResizable(true);
        
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
        
        
        saveManager = new SaveManager();
        
        mainMenu = new MainMenu(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        mainMenu.setMenuActionListener(this); // clearly set the listener!
        creditsScreen = new CreditsScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT, CREDITS_TOTAL_DURATION, CREDITS_FADE_DURATION);
        
        pauseScreen = new PauseScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        
        gameOverScreen = new GameOverScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        
        loadGameScreen = new LoadGameScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT, this);
        
        savePopup = new SavePopup(menuFont); // scaled size
       
        
        camera = new Camera(new Size(INTERNAL_WIDTH, INTERNAL_HEIGHT), forest.getBounds());
                
        camera.focusOn(player);

        gameStateManager = new GameStateManager(); // Start at MAIN_MENU
        
        canvas.addKeyListener(this);
        
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = translateMousePoint(e.getPoint());

                if (gameStateManager.is(GameState.MAIN_MENU)) {
                    // Adjust coordinates for scaling/fullscreen clearly
                    mainMenu.mouseClicked(new MouseEvent(
                        e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), p.x, p.y, e.getClickCount(), e.isPopupTrigger()
                    ));
                } else if (gameStateManager.is(GameState.NEW_GAME)) {
                    newGameScreen.mouseClicked(p);
                }  else if (gameStateManager.is(GameState.LOAD_GAME)) {
                	loadGameScreen.mouseClicked(e); // pass the original MouseEvent
                } else if (gameStateManager.is(GameState.PAUSED)) {
                    pauseScreen.mouseClicked(adjustMouseEvent(e, p), Game.this);
                }
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = translateMousePoint(e.getPoint());

            	if (gameStateManager.is(GameState.MAIN_MENU)) {
                    mainMenu.mouseMoved(new MouseEvent(
                        e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), p.x, p.y, e.getClickCount(), e.isPopupTrigger()
                    ));
                } else if (gameStateManager.is(GameState.NEW_GAME)) {
                    newGameScreen.mouseMoved(p);
                }  else if (gameStateManager.is(GameState.LOAD_GAME)) {
                	loadGameScreen.mouseClicked(e); // pass the original MouseEvent
                } else if (gameStateManager.is(GameState.PAUSED)) {
                    pauseScreen.mouseMoved(adjustMouseEvent(e, p));
                }
            } 
        });
    }
    
    public void setPendingSaveName(String name) {
        this.pendingSaveName = name;
    }

    public synchronized void start() {
        running = true;
        new Thread(this).start();
    }
    
    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
    
    public Player getPlayer() {
        return player;
    }

    public synchronized void stop() {
        running = false;
    }
    
    private MouseEvent adjustMouseEvent(MouseEvent e, Point p) {
        return new MouseEvent(
            e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
            p.x, p.y, e.getClickCount(), e.isPopupTrigger()
        );
    }
    
    public void setCurrentSaveSlot(int slot) {
        this.currentSaveSlot = slot;
    }

    public int getCurrentSaveSlot() {
        return currentSaveSlot;
    }
    
    private void populateObjectsFromTileMap(GameMap map, TileMap tileMap) {
        char[][] layout = tileMap.getLayout();
        float startX = map.getBounds().getMinX();
        float startY = map.getBounds().getMinY();

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                char tile = layout[y][x];
                float worldX = startX + x * 16;
                float worldY = startY + y * 16;

                switch (tile) {
                    case 't':
                        // Only place a tall tree if there is a 'T' trunk directly below
                        if (y + 1 < layout.length && layout[y + 1][x] == 'T') {
                            map.addObject(new Tree(new Position(worldX, worldY))); // Tree top at (x, y)
                            layout[y][x] = '.';     // Clear top
                            layout[y + 1][x] = '.'; // Clear trunk
                        }
                        break;
                    case 'n':
                        map.addObject(new Npc(new Position(worldX, worldY)));
                        layout[y][x] = '.';
                        break;
                    case 'f':
                        map.addObject(new Flower(new Position(worldX, worldY)));
                        layout[y][x] = '.';
                        break;
                    case 'L':
                        map.addDoor(new Door((int)worldX, (int)worldY, TileMap.TILE_SIZE, TileMap.TILE_SIZE, "lake", 2 * TileMap.TILE_SIZE, 2 * TileMap.TILE_SIZE));
                        layout[y][x] = '.';
                        break;
                    case 'F':
                        map.addDoor(new Door((int)worldX, (int)worldY, TileMap.TILE_SIZE, TileMap.TILE_SIZE, "forest", 7 * TileMap.TILE_SIZE, 3 * TileMap.TILE_SIZE));
                        layout[y][x] = '.';
                        break;
                    case 'C':
                        map.addDoor(new Door((int)worldX, (int)worldY, TileMap.TILE_SIZE, TileMap.TILE_SIZE, "cemetery", 1 * TileMap.TILE_SIZE, 1 * TileMap.TILE_SIZE));
                        layout[y][x] = '.';
                        break;
                }
            }
        }
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
        
        // ✅ Adicione aqui:
        if (gameStateManager.is(GameState.NEW_GAME)) {
            newGameScreen.tick(deltaTime);
        }
        
        if (gameStateManager.is(GameState.LOAD_GAME)) {
            // If your LoadGameScreen needs a tick/update in future, add here
            // loadGameScreen.tick(deltaTime); ← optional
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
        	if (teleportCooldown > 0) {
        	    teleportCooldown--;
        	} else {
        	    justTeleported = false;
        	}

        	// Door transitions
        	if (!justTeleported) {
        	    for (Door door : mapManager.getCurrentMap().getDoors()) {
        	        if (door.intersects(player.getPosition())) {
        	            mapManager.setCurrentMap(door.getTargetMapId());
        	            float offsetX = 0, offsetY = 0;
        	         // Push player slightly away from door so it doesn't instantly re-enter
        	            if (door.getTargetX() == 0) offsetX = 20; // coming from left
        	            else if (door.getTargetX() + door.getWidth() >= mapManager.getCurrentMap().getBounds().getMaxX()) offsetX = -20; // right

        	            player.setPosition(new Position(door.getTargetX() + offsetX, door.getTargetY() + offsetY));        	            camera = new Camera(new Size(INTERNAL_WIDTH, INTERNAL_HEIGHT), mapManager.getCurrentMap().getBounds());
        	            camera.focusOn(player);

        	            justTeleported = true;
        	            teleportCooldown = 20; // ~0.3s delay (if 60 ticks/sec)
        	            break;
        	        }
        	    }
        	}
            updatePlayerVelocity();
            player.update(deltaTime);

            // Clamp player to map bounds
            Position playerPos = player.getPosition();
            MapBounds bounds = mapManager.getCurrentMap().getBounds();
            float clampedX = Math.max(bounds.getMinX(), Math.min(bounds.getMaxX() - 16, player.getPosition().getX()));
            float clampedY = Math.max(bounds.getMinY(), Math.min(bounds.getMaxY() - 16, player.getPosition().getY()));
            player.getPosition().setX(clampedX);
            player.getPosition().setY(clampedY);

            camera.update();
        }
        
        if (gameStateManager.is(GameState.PAUSED)) {
            pauseScreen.update();
        }
        
        if (gameStateManager.is(GameState.GAME_OVER)) {
            gameOverScreen.update();
        }
        
        if (gameStateManager.is(GameState.INTRO)) {
            introScreen.update();
        }
    }
    
    public void requestRender() {
        render(); // chama a renderização imediata (dentro do loop de jogo, isso é seguro)
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
        if (!gameStateManager.is(GameState.PAUSED)) {
        	g.setColor(Color.BLACK);
            g.fillRect(0, 0, INTERNAL_WIDTH, INTERNAL_HEIGHT);
        }
        
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
                // Translate and scale camera first
                Position camPos = camera.getPosition();
                g.scale(camera.getZoom(), camera.getZoom());
                g.translate(-camPos.getX(), -camPos.getY());

                // ✅ Now render map in world space
                mapManager.getCurrentMap().render(g, camera);

//                // Draw grid lines
//                int gridSize = 16;
//                g.setColor(new Color(255, 255, 255, 40));
//                MapBounds bounds = mapManager.getCurrentMap().getBounds();
//
//                int startX = (int) bounds.getMinX();
//                int endX = (int) bounds.getMaxX();
//                int startU = (int) bounds.getMinY();
//                int endY = (int) bounds.getMaxY();
//
//                for (int x = startX; x <= endX; x += gridSize) {
//                    g.drawLine(x, startU, x, endY);
//                }
//
//                for (int y = startU; y <= endY; y += gridSize) {
//                    g.drawLine(startX, y, endX, y);
//                }

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
            case NEW_GAME:
                newGameScreen.render(g);
                break;
            case LOAD_GAME:
                loadGameScreen.render(g);
                break;
            case INTRO:
                introScreen.render(g);
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
        
        savePopup.render(gFinal, canvas.getWidth(), canvas.getHeight());

        gFinal.dispose();
        bs.show();
    }
    
    public int getStoryProgress() {
        return storyProgress;
    }

    public void setStoryProgress(int progress) {
        this.storyProgress = progress;
    }

    public void startIntro(String saveName) {
        introScreen = new IntroScreen(this, menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT, saveName);
        gameStateManager.setState(GameState.INTRO);
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
    
    public void quickSave(String flagKey, String flagValue) {
        int slot = getCurrentSaveSlot();
        if (slot == -1) {
            System.out.println("No active slot to save.");
            return;
        }

        Position pos = player.getPosition();
        SaveData data = saveManager.loadFromSlot(slot);
        if (data == null) {
            System.out.println("Could not load data for slot " + slot);
            return;
        }

        data.setChoice(flagKey, flagValue);
        data.storyProgress = storyProgress; // assumes storyProgress is tracked in Game
        data.playerX = pos.getX();
        data.playerY = pos.getY();

        saveManager.saveToSlot(slot, data);

        int percent = SaveUtils.calculateProgressPercentage(storyProgress);
        savePopup.show("Progress Saved: " + percent + "%");
        System.out.println("Saved to slot " + slot + " — " + percent + "% complete");
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        
        if (key == KeyEvent.VK_F11)
            setFullscreen(!fullscreen);
        
        // Handle PAUSE MENU input first
        if (gameStateManager.is(GameState.PAUSED)) {
            pauseScreen.keyPressed(e, this);
            return; // 🔁 Return early so other states aren't processed
        }

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

            if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
                if (gameStateManager.is(GameState.PLAYING)) {
                    // 🟢 Create snapshot at *actual screen size*
                    int screenW = canvas.getWidth();
                    int screenH = canvas.getHeight();
                    BufferedImage snapshot = new BufferedImage(screenW, screenH, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gSnapshot = snapshot.createGraphics();

                    // Same rendering hints as final output
                    gSnapshot.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    gSnapshot.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                    gSnapshot.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                    // Fill black background
                    gSnapshot.setColor(Color.BLACK);
                    gSnapshot.fillRect(0, 0, screenW, screenH);

                    // 🟢 Scale the internal buffer as done in render()
                    double scaleX = screenW / (double) INTERNAL_WIDTH;
                    double scaleY = screenH / (double) INTERNAL_HEIGHT;
                    double scale = Math.min(scaleX, scaleY);

                    int finalW = (int) (INTERNAL_WIDTH * scale);
                    int finalH = (int) (INTERNAL_HEIGHT * scale);
                    int xOffset = (screenW - finalW) / 2;
                    int yOffset = (screenH - finalH) / 2;

                    // First render the internal world buffer
                    BufferedImage worldImage = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
                    Graphics2D gWorld = worldImage.createGraphics();

                    // Your world rendering logic at INTERNAL resolution
                    gWorld.setColor(Color.BLACK);
                    gWorld.fillRect(0, 0, INTERNAL_WIDTH, INTERNAL_HEIGHT);

                    Position camPos = camera.getPosition();
                    gWorld.scale(camera.getZoom(), camera.getZoom());
                    gWorld.translate(-camPos.getX(), -camPos.getY());

                    // Grid
                    int gridSize = 16;
                    MapBounds bounds = mapManager.getCurrentMap().getBounds();
                    for (int x = 0; x <= bounds.getMaxX(); x += gridSize)
                        gWorld.drawLine(x, 0, x, (int) bounds.getMaxY());
                    for (int y = 0; y <= bounds.getMaxY(); y += gridSize)
                        gWorld.drawLine(0, y, (int) bounds.getMaxX(), y);

                    // Player
                    player.render(gWorld);
                    gWorld.dispose();

                    // 🟢 Draw world image scaled onto snapshot
                    gSnapshot.drawImage(worldImage, xOffset, yOffset, finalW, finalH, null);
                    gSnapshot.dispose();

                    pauseScreen.setBackgroundSnapshot(snapshot);
                    gameStateManager.setState(GameState.PAUSED);
                    pauseScreen.resetFade();
                }
            }

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
            
            if (key == KeyEvent.VK_J) {
                quickSave("pressed_J", "true");
            }
            
            updatePlayerVelocity();
        }
        
        if (gameStateManager.is(GameState.NEW_GAME)) {
            newGameScreen.keyPressed(e);
            return;
        }
        
        if (gameStateManager.is(GameState.LOAD_GAME)) {
            loadGameScreen.keyPressed(e);
            return;
        }
        
        if (gameStateManager.is(GameState.INTRO)) {
            introScreen.keyPressed(e.getKeyCode());
            return;
        }
    }
    
    private void renderGameToSnapshot(Graphics2D g) {
        Position camPos = camera.getPosition();
        g.scale(camera.getZoom(), camera.getZoom());
        g.translate(-camPos.getX(), -camPos.getY());

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, INTERNAL_WIDTH, INTERNAL_HEIGHT);

        // Grid
        int gridSize = 16;
        g.setColor(new Color(255, 255, 255, 40));
        int startX = (int) (Math.max(camPos.getX(), forestBounds.getMinX()) - (camPos.getX() % gridSize)) - gridSize;
        int endX = (int) (Math.min(camPos.getX() + INTERNAL_WIDTH, forestBounds.getMaxX())) + gridSize;
        int startY = (int) (Math.max(camPos.getY(), forestBounds.getMinY()) - (camPos.getY() % gridSize)) - gridSize;
        int endY = (int) (Math.min(camPos.getY() + INTERNAL_HEIGHT, forestBounds.getMaxY())) + gridSize;

        for (int x = startX; x <= endX; x += gridSize)
            g.drawLine(x, startY, x, endY);
        for (int y = startY; y <= endY; y += gridSize)
            g.drawLine(startX, y, endX, y);

        // Player
        player.render(g);
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
        
        if (gameStateManager.is(GameState.INTRO)) {
            introScreen.keyReleased(e.getKeyCode());
            return;
        }
        updatePlayerVelocity();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (gameStateManager.is(GameState.NEW_GAME)) {
            newGameScreen.keyTyped(e);
        }
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

        if (pendingSaveName != null && player != null) {
        	int slotToUse = getNextAvailableSlot();
        	setCurrentSaveSlot(slotToUse); // ✅ Track it!

        	Position pos = player.getPosition();
        	SaveData data = new SaveData(pendingSaveName, pos.getX(), pos.getY());
        	saveManager.saveToSlot(slotToUse, data);

            // ✅ Set message and timestamp
            savePopup.show("Saved to Slot " + slotToUse);

            pendingSaveName = null;
        }

        System.out.println("Game Started!");
    }
    
    private int getNextAvailableSlot() {
        for (int i = 1; i <= 3; i++) {
            if (!saveManager.hasSave(i)) return i;
        }
        return 1; // If all slots are full, overwrite Slot 1
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
        newGameScreen = new NewGameScreen(menuFont, INTERNAL_WIDTH, INTERNAL_HEIGHT, this);
        gameStateManager.setState(GameState.NEW_GAME);
    }
    
    @Override
    public void onLoadGame() {
        loadGameScreen.refreshSlots();
        gameStateManager.setState(GameState.LOAD_GAME);
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
