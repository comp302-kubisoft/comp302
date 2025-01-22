import static org.junit.jupiter.api.Assertions.*;

import domain.model.GameState;
import domain.model.entity.Hero;
import domain.model.entity.Monster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.tile.TileManager;

public class HeroTest {
  private GameState gameState;
  private Hero hero;
  private TileManager tileManager;

  @BeforeEach
  void setUp() {
    gameState = new GameState(48, 20, 20);
    hero = gameState.getHero();
    tileManager = gameState.getTileManager();
  }

  @Test
  void testHeroWallCollision() {
    // Try to move into a wall
    hero.moveIfPossible(0, 48, tileManager, 48); // Move one tile down
    int initialY = hero.getY();

    // Place wall directly below hero
    int heroGridX = hero.getX() / 48;
    int heroGridY = (hero.getY() / 48) + 1;
    tileManager.mapTileNum[heroGridX][heroGridY] = 1; // 1 is wall tile

    // Try to move into wall
    hero.moveIfPossible(0, 48, tileManager, 48);

    // Hero should not have moved
    assertEquals(initialY, hero.getY(), "Hero should not move through walls");
  }

  @Test
  void testHeroMonsterCollision() {
    // Get hero's initial position
    int initialX = hero.getX();
    int initialY = hero.getY();

    // Add monster at adjacent tile
    gameState.addMonster(
        new Monster(
            Monster.Type.FIGHTER,
            initialX + 48, // One tile to the right
            initialY));

    // Try to move into monster's position
    hero.moveIfPossible(48, 0, tileManager, 48); // Try to move right

    // Hero should not have moved
    assertEquals(initialX, hero.getX(), "Hero should not move into monster's position");
    assertEquals(initialY, hero.getY(), "Hero's Y position should remain unchanged");
  }

  @Test
  void testHeroObjectCollision() {
    // Get hero's initial position
    int initialX = hero.getX();
    int initialY = hero.getY();

    // Place an object one tile to the right of hero
    int heroGridX = (hero.getX() / 48) + 1;
    int heroGridY = hero.getY() / 48;
    gameState.addPlacedObject(0, heroGridX * 48, heroGridY * 48, heroGridX, heroGridY);

    // Try to move into object's position
    hero.moveIfPossible(48, 0, tileManager, 48); // Try to move right

    // Hero should not have moved
    assertEquals(initialX, hero.getX(), "Hero should not move into object's position");
    assertEquals(initialY, hero.getY(), "Hero's Y position should remain unchanged");
  }
}
