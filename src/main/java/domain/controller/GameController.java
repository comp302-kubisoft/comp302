package domain.controller;

import domain.model.GameState;
import domain.model.entity.Hero;
import ui.input.InputState;
import ui.main.GamePanel;

public class GameController {

  private final GameState gameState;
  private final InputState inputState;
  private final GamePanel gamePanel;
  private boolean spawnPositionSet = false;

  public GameController(GameState gameState, InputState inputState, GamePanel gamePanel) {
    this.gameState = gameState;
    this.inputState = inputState;
    this.gamePanel = gamePanel;
  }

  public void update() {
    Hero hero = gameState.getHero();

    if (!spawnPositionSet) {
      hero.setSpawnPosition(gameState.getTileManager(), gamePanel.getTileSize());
      spawnPositionSet = true;
    }

    int dx = 0, dy = 0;
    String direction = hero.getDirection();

    if (inputState.upPressed) {
      direction = "up";
      dy = -hero.getSpeed();
    } else if (inputState.downPressed) {
      direction = "down";
      dy = hero.getSpeed();
    } else if (inputState.leftPressed) {
      direction = "left";
      dx = -hero.getSpeed();
    } else if (inputState.rightPressed) {
      direction = "right";
      dx = hero.getSpeed();
    }

    hero.setDirection(direction);
    hero.moveIfPossible(dx, dy, gameState.getTileManager(), gamePanel.getTileSize());
  }
}
