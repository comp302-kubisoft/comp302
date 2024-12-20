package domain.model;

import domain.model.entity.Hero;
import ui.main.GamePanel;
import ui.tile.TileManager;

public class GameState {

  private Hero hero;
  private TileManager tileManager;

  public GameState(GamePanel gamePanel) {
    this.hero = new Hero();
    this.tileManager = new TileManager(gamePanel);
  }

  public Hero getHero() {
    return hero;
  }

  public TileManager getTileManager() {
    return tileManager;
  }
}
