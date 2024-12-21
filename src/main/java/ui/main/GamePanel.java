/**
 * The main game panel that serves as the primary container for the game.
 * This class handles the game loop, rendering, and coordinates between different game components.
 * It extends JPanel and implements Runnable to support the game loop.
 */
package ui.main;

import domain.controller.GameController;
import domain.model.GameMode;
import domain.model.GameState;
import ui.input.InputState;
import ui.input.KeyHandler;
import ui.input.MouseHandler;
import ui.render.Renderer;
import ui.menu.Menu;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    /** Original tile size before scaling */
    final int originalTileSize = 16;
    /** Scale factor for rendering */
    final int scale = 3;
    /** Actual tile size used in game (originalTileSize * scale) */
    public final int tileSize = originalTileSize * scale;
    /** Number of columns in the game screen */
    public final int maxScreenCol = 28;
    /** Number of rows in the game screen */
    public final int maxScreenRow = 20;
    /** Total width of the game screen in pixels */
    public final int screenWidth = tileSize * maxScreenCol;
    /** Total height of the game screen in pixels */
    public final int screenHeight = tileSize * maxScreenRow;
    /** Target frames per second */
    private static final double FPS = 60.0;
    /** Time interval between frames in nanoseconds */
    private static final double DRAW_INTERVAL = 1000000000 / FPS;

    /** Handles keyboard input state */
    InputState inputState;
    /** Processes keyboard events */
    KeyHandler keyH;
    /** Processes mouse events */
    MouseHandler mouseH;
    /** Main game loop thread */
    Thread gameThread;
    /** Maintains the current state of the game */
    public GameState gameState;
    /** Handles all game rendering */
    Renderer renderer;
    /** Controls game logic and updates */
    GameController gameController;
    /** Manages the game menu */
    private Menu menu;

    /** Current mode of the game (MENU, PLAY, BUILD, HELP) */
    private GameMode currentMode = GameMode.MENU;

    /**
     * Initializes the game panel and all its components.
     * Sets up the display, input handlers, game state, renderer, and controllers.
     */
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);

        this.inputState = new InputState();
        this.keyH = new KeyHandler(inputState);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        this.gameState = new GameState(tileSize, maxScreenCol, maxScreenRow);
        this.renderer = new Renderer(gameState, tileSize, screenWidth, screenHeight);
        this.menu = new Menu();
        this.renderer.setMenu(menu);
        this.gameController = new GameController(gameState, inputState, this);

        this.mouseH = new MouseHandler(renderer, gameState, tileSize, screenWidth);
        this.addMouseListener(mouseH);
    }

    /**
     * Returns the current tile size used in the game.
     * 
     * @return The size of a single tile in pixels
     */
    public int getTileSize() {
        return this.tileSize;
    }

    /**
     * Starts the game thread and begins the game loop.
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * Sets the current game mode.
     * 
     * @param mode The new GameMode to switch to
     */
    public void setMode(GameMode mode) {
        this.currentMode = mode;
        if (mode == GameMode.BUILD) {
            gameController.moveToNextHall(); // Transition to the next hall when Build Mode is activated
        }
    }

    /**
     * Gets the current game mode.
     * 
     * @return The current GameMode
     */
    public GameMode getMode() {
        return currentMode;
    }

    /**
     * Gets the current renderer instance.
     * 
     * @return The Renderer object handling game rendering
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Main game loop implementation.
     * Handles timing, updates, and rendering at the specified FPS.
     */
    @Override
    public void run() {
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / DRAW_INTERVAL;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    /**
     * Updates the game state based on the current game mode.
     * Delegates to appropriate controller methods for each mode.
     */
    public void update() {
        switch (currentMode) {
            case MENU:
            case HELP:
                gameController.updateMenuOrHelpMode();
                break;
            case PLAY:
                gameController.updatePlayMode();
                break;
            case BUILD:
                gameController.updateBuildMode();
                break;
        }
    }

    /**
     * Handles the rendering of the game.
     * Sets up graphics context and delegates rendering to the Renderer.
     * 
     * @param g The Graphics context provided by Swing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        renderer.render(g2, this.currentMode);

        g2.dispose();
    }

    /**
     * Resets the game state to its initial condition.
     * Reinitializes all components while preserving the menu state.
     * Used when returning to menu or starting a new game.
     */
    public void resetGameState() {
        // Re-initialize the game state
        this.gameState = new GameState(tileSize, maxScreenCol, maxScreenRow);

        // Re-initialize the renderer with the new game state
        this.renderer = new Renderer(gameState, tileSize, screenWidth, screenHeight);
        this.renderer.setMenu(menu); // Keep the menu reference

        // Re-initialize the game controller
        this.gameController = new GameController(gameState, inputState, this);

        // Re-initialize the mouse handler with the new renderer
        this.mouseH = new MouseHandler(renderer, gameState, tileSize, screenWidth);
        // Remove old mouse listener and add new one
        this.removeMouseListener(this.getMouseListeners()[0]);
        this.addMouseListener(mouseH);
    }
}