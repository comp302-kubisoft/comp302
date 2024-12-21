/**
 * Represents the player character in the game.
 * Handles hero movement, collision detection, and sprite management.
 * Extends the base Entity class with hero-specific functionality.
 */
package domain.model.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import ui.tile.TileManager;
import domain.model.GameState;
import java.util.Random;

public class Hero extends Entity {

  /** The hero's sprite image */
  private BufferedImage image;
  /** Tracks if the initial spawn position has been set */
  private boolean spawnPositionSet = false;
  /** Default movement speed of the hero */
  private static final int DEFAULT_SPEED = 4;
  /** Reference to the game state for collision detection */
  private GameState gameState;
  /** Random number generator for spawn position */
  private static final Random random = new Random();

  /**
   * Creates a new hero instance with a reference to the game state.
   * 
   * @param gameState Reference to the current game state for collision detection
   */
  public Hero(GameState gameState) {
    this.gameState = gameState;
    setDefaultValues();
    loadImage();
  }

  /**
   * Sets the initial default values for the hero.
   * Initializes position, speed, and direction.
   */
  private void setDefaultValues() {
    setSpeed(DEFAULT_SPEED);
    direction = "down";
    x = 0;
    y = 0;
  }

  /**
   * Sets the hero's spawn position to a random valid location.
   * Ensures the spawn point is within the game area and not colliding with
   * objects.
   * 
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize    Size of each tile in pixels
   */
  public void setSpawnPosition(TileManager tileManager, int tileSize) {
    if (spawnPositionSet)
      return;

    // Get game area boundaries from GameState
    int minX = gameState.getGameAreaStart();
    int maxX = gameState.getGameAreaEnd();
    int minY = gameState.getGameAreaStart();
    int maxY = gameState.getGameAreaEnd();

    // Keep trying random positions until we find a valid one
    while (!spawnPositionSet) {
      // Generate random position within game area
      int gridX = minX + random.nextInt(maxX - minX + 1);
      int gridY = minY + random.nextInt(maxY - minY + 1);

      int currentHallIndex = gameState.getCurrentHallIndex();
      
      // Check if position is valid (no wall or object)
      if (!tileManager.tile[tileManager.mapTileNum[gridX][gridY]].collision &&
          !gameState.isTileOccupied(gridX, gridY, currentHallIndex)) {

        // Convert grid position to pixel coordinates
        x = gridX * tileSize;
        y = gridY * tileSize;
        spawnPositionSet = true;
      }
    }
  }

  /**
   * Attempts to move the hero by the specified amount.
   * Checks for collisions before allowing movement.
   * 
   * @param dx          Change in x position
   * @param dy          Change in y position
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize    Size of each tile in pixels
   */
  public void moveIfPossible(int dx, int dy, TileManager tileManager, int tileSize) {
    if (dx != 0) {
      int newX = x + dx;
      if (!checkCollision(newX, y, tileManager, tileSize)) {
        x = newX;
      }
    }

    if (dy != 0) {
      int newY = y + dy;
      if (!checkCollision(x, newY, tileManager, tileSize)) {
        y = newY;
      }
    }
  }

  /**
   * Checks if a proposed position would result in a collision.
   * Considers both wall tiles and placed objects.
   * 
   * @param newX        Proposed x position
   * @param newY        Proposed y position
   * @param tileManager Reference to the tile manager
   * @param tileSize    Size of each tile in pixels
   * @return true if there would be a collision, false if position is valid
   */
  private boolean checkCollision(int newX, int newY, TileManager tileManager, int tileSize) {
    // Check wall collisions
    if (tileManager.checkTileCollision(newX, newY, tileSize, tileSize)) {
      return true;
    }

    // Get grid positions for all corners of the hero
    int leftTile = newX / tileSize;
    int rightTile = (newX + tileSize - 1) / tileSize;
    int topTile = newY / tileSize;
    int bottomTile = (newY + tileSize - 1) / tileSize;

    // Fetch the current hall index
    int currentHallIndex = gameState.getCurrentHallIndex();
    
    // Check object collisions for all tiles the hero might overlap
    for (GameState.PlacedObject obj : gameState.getObjectsForHall(currentHallIndex)) {
      if ((obj.gridX >= leftTile && obj.gridX <= rightTile) &&
          (obj.gridY >= topTile && obj.gridY <= bottomTile)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Loads the hero's sprite image from resources.
   */
  private void loadImage() {
    try {
      image = ImageIO.read(getClass().getResourceAsStream("/hero/player.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the hero's sprite image.
   * 
   * @return The BufferedImage representing the hero
   */
  public BufferedImage getImage() {
    return image;
  }

  /**
   * Gets the hero's current x position.
   * 
   * @return Current x coordinate in pixels
   */
  public int getX() {
    return x;
  }

  /**
   * Gets the hero's current y position.
   * 
   * @return Current y coordinate in pixels
   */
  public int getY() {
    return y;
  }

  /**
   * Gets the hero's movement speed.
   * 
   * @return Current movement speed in pixels per update
   */
  public int getSpeed() {
    return speed;
  }

  /**
   * Gets the hero's current facing direction.
   * 
   * @return String indicating the direction ("up", "down", "left", "right")
   */
  public String getDirection() {
    return direction;
  }

  /**
   * Sets the hero's facing direction.
   * 
   * @param direction New direction to face ("up", "down", "left", "right")
   */
  public void setDirection(String direction) {
    this.direction = direction;
  }
}
