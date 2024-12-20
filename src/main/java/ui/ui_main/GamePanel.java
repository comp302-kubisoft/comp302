package ui.ui_main;

import domain.entity.Hero;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import ui.tile.TileManager;

/**
 * Represents the main game panel where all game logic, rendering, and updates occur. Implements the
 * game loop and manages the hero, tiles, and input handling.
 */
public class GamePanel extends JPanel implements Runnable {

  // Configuration for the game grid
  final int originalTileSize = 16; // 16x16 pixel tile
  final int scale = 3;
  public final int tileSize = originalTileSize * scale; // Scaled tile size (48x48 pixels)
  public final int maxScreenCol = 16; // Maximum number of columns on the screen
  public final int maxScreenRow = 12; // Maximum number of rows on the screen
  public final int screenWidth = tileSize * maxScreenCol; // Screen width in pixels
  public final int screenHeight = tileSize * maxScreenRow; // Screen height in pixels

  int FPS = 60; // Target frames per second

  // Managers and components
  InputState inputState; // INPUTSTATE INSTANCE ADDED
  KeyHandler keyH;       // KEYHANDLER UPDATED
  Thread gameThread;     // Game loop thread
  public GameState gameState;   // GAMESTATE INSTANCE
  Renderer renderer;     // RENDERER INSTANCE

  /** Constructs the game panel, initializing its size, background, and input handling. */
  public GamePanel() {
    this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // Set preferred dimensions
    this.setBackground(Color.black); // Set background color to black
    this.setDoubleBuffered(true); // Enable double buffering for smoother rendering

    // INITIALIZE INPUTSTATE
    this.inputState = new InputState(); // INPUTSTATE INITIALIZED

    // INITIALIZE KEYHANDLER WITH INPUTSTATE
    this.keyH = new KeyHandler(inputState); // KEYHANDLER UPDATED TO USE INPUTSTATE

    this.addKeyListener(keyH); // Add the key listener for handling input
    this.setFocusable(true); // Ensure the panel can receive key events

    // INITIALIZE GAMESTATE
    this.gameState = new GameState(this, keyH);

    // INITIALIZE RENDERER
    this.renderer = new Renderer(gameState);
  }

  /** Starts the game thread, initializing the game loop. */
  public void startGameThread() {
    gameThread = new Thread(this);
    gameThread.start();
  }

  @Override
  public void run() {

    double drawInterval = 1000000000 / FPS; // Interval between frames in nanoseconds
    double delta = 0;
    long lastTime = System.nanoTime();
    long currentTime;

    // Main game loop
    while (gameThread != null) {

      currentTime = System.nanoTime();
      delta += (currentTime - lastTime) / drawInterval;
      lastTime = currentTime;

      if (delta >= 1) {
        update(); // Update game state
        repaint(); // Request re-rendering
        delta--;
      }
    }
  }

  /** Updates the game state. */
  public void update() {
    // DELEGATE UPDATES TO GAMESTATE
    gameState.update();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    // DELEGATE RENDERING TO THE RENDERER
    renderer.render(g2); // USE RENDERER TO DRAW GAMESTATE COMPONENTS

    g2.dispose(); // Release graphics resources
  }
}
