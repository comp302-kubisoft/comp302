package ui.main;

import domain.controller.GameController;
import domain.model.GameMode;
import domain.model.GameState;
import ui.input.InputState;
import ui.input.KeyHandler;
import ui.render.Renderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 28;
    public final int maxScreenRow = 20;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;
    private static final double FPS = 60.0;
    private static final double DRAW_INTERVAL = 1000000000 / FPS;

    InputState inputState;
    KeyHandler keyH;
    Thread gameThread;
    public GameState gameState;
    Renderer renderer;
    GameController gameController;

    private GameMode currentMode = GameMode.MENU;

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
        this.gameController = new GameController(gameState, inputState, this);
    }

    public int getTileSize() {
        return this.tileSize;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setMode(GameMode mode) {
        this.currentMode = mode;
    }

    public GameMode getMode() {
        return currentMode;
    }

    public Renderer getRenderer() {
        return renderer;
    }

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

    public void update() {
        switch (currentMode) {
            case MENU:
            case HELP:
                gameController.updateMenuOrHelpMode();
                break;
            case PLAY:
                gameController.updatePlayMode();
                break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        renderer.render(g2, this.currentMode);

        g2.dispose();
    }

    public void resetGameState() {
        // Re-initialize the game state
        this.gameState = new GameState(tileSize, maxScreenCol, maxScreenRow);

        // Re-initialize the renderer with the new game state
        this.renderer = new Renderer(gameState, tileSize, screenWidth, screenHeight);

        // Re-initialize the game controller
        this.gameController = new GameController(gameState, inputState, this);
    }

}