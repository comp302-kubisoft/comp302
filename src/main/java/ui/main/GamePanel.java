package ui.main;

import domain.controller.GameController;
import domain.model.GameState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import ui.input.InputState;
import ui.input.KeyHandler;
import ui.render.Renderer;

public class GamePanel extends JPanel implements Runnable {

  final int originalTileSize = 16;
  final int scale = 3;
  public final int tileSize = originalTileSize * scale;
  public final int maxScreenCol = 16;
  public final int maxScreenRow = 12;
  public final int screenWidth = tileSize * maxScreenCol;
  public final int screenHeight = tileSize * maxScreenRow;
  int FPS = 60;

  InputState inputState;
  KeyHandler keyH;
  Thread gameThread;
  GameState gameState;
  Renderer renderer;
  GameController gameController;

  public GamePanel() {
    this.setPreferredSize(new Dimension(screenWidth, screenHeight));
    this.setBackground(Color.black);
    this.setDoubleBuffered(true);

    this.inputState = new InputState();
    this.keyH = new KeyHandler(inputState);
    this.addKeyListener(keyH);
    this.setFocusable(true);

    this.gameState = new GameState(this);
    this.renderer = new Renderer(gameState, tileSize);

    this.gameController = new GameController(gameState, inputState, this);
  }

  public int getTileSize() {
    return this.tileSize;
  }

  public void startGameThread() {
    gameThread = new Thread(this);
    gameThread.start();
  }

  @Override
  public void run() {
    double drawInterval = 1000000000 / FPS;
    double delta = 0;
    long lastTime = System.nanoTime();

    while (gameThread != null) {
      long currentTime = System.nanoTime();
      delta += (currentTime - lastTime) / drawInterval;
      lastTime = currentTime;

      if (delta >= 1) {
        update();
        repaint();
        delta--;
      }
    }
  }

  public void update() {

    gameController.update();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    renderer.render(g2);
    g2.dispose();
  }
}
