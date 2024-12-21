package domain.model;

import domain.model.entity.Hero;
import ui.tile.TileManager;

public class GameState {

  private Hero hero;
  private TileManager tileManager;

  public GameState(int tileSize, int maxScreenCol, int maxScreenRow) {
    this.hero = new Hero();
    this.tileManager = new TileManager(tileSize, maxScreenCol, maxScreenRow);
  }

  public Hero getHero() {
    return hero;
  }

  public TileManager getTileManager() {
    return tileManager;
  }
}
