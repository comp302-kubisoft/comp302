package ui.render;

import domain.model.GameState;
import java.awt.Graphics2D;

public class Renderer {

  private GameState gameState;

  private int tileSize;

  public Renderer(GameState gameState, int tileSize) {
    this.gameState = gameState;
    this.tileSize = tileSize;
  }

  public void render(Graphics2D g2) {

    gameState.getTileManager().draw(g2);

    g2.drawImage(
        gameState.getHero().getImage(),
        gameState.getHero().getX(),
        gameState.getHero().getY(),
        tileSize,
        tileSize,
        null);
  }
}
