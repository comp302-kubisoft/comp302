/**
 * Represents the player character in the game. Handles hero movement, collision detection, and
 * sprite management. Extends the base Entity class with hero-specific functionality.
 */
package domain.model.entity;

import domain.model.GameState;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import ui.tile.TileManager;

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

  /** Current health of the hero */
  private int health;

  /** Maximum possible health of the hero */
  private static final int MAX_HEALTH = 4;

  /** Starting health of the hero */
  private static final int STARTING_HEALTH = 3;

  /** Add these fields for animation */
  private int spriteCounter = 0;

  private int spriteNum = 1;
  private static final int SPRITE_CHANGE_RATE = 12; // Adjust this to control animation speed

  /** Add collision box constants */
  private static final double COLLISION_BOX_WIDTH = 0.5; // 8 pixels (50% of 16)

  private static final double COLLISION_BOX_HEIGHT = 0.375; // 6 pixels (37.5% of 16)
  private static final double COLLISION_BOX_X_OFFSET = 0.25; // Center horizontally (25% from left)
  private static final double COLLISION_BOX_Y_OFFSET = 0.625; // Place at bottom (62.5% from top)

  /** Add damage effect fields */
  private boolean isDamaged = false;

  private long damageEffectStartTime;
  private static final long DAMAGE_EFFECT_DURATION = 500; // 0.5 seconds of red flash

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
   * Sets the initial default values for the hero. Initializes position, speed, direction, and
   * health.
   */
  private void setDefaultValues() {
    setSpeed(DEFAULT_SPEED);
    direction = "down";
    x = 0;
    y = 0;
    health = STARTING_HEALTH;
  }

  /**
   * Sets the hero's spawn position to a random valid location. Ensures the spawn point is within
   * the game area and not colliding with objects.
   *
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize Size of each tile in pixels
   */
  public void setSpawnPosition(TileManager tileManager, int tileSize) {
    if (spawnPositionSet) return;

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

      // Check if position is valid (no wall or object)
      if (!tileManager.tile[tileManager.mapTileNum[gridX][gridY]].collision
          && !gameState.isTileOccupied(gridX, gridY)) {

        // Convert grid position to pixel coordinates
        x = gridX * tileSize;
        y = gridY * tileSize;
        spawnPositionSet = true;
      }
    }
  }

  /**
   * Attempts to move the hero by the specified amount. Checks for collisions before allowing
   * movement. Updates animation and direction.
   *
   * @param dx Change in x position
   * @param dy Change in y position
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize Size of each tile in pixels
   */
  public void moveIfPossible(int dx, int dy, TileManager tileManager, int tileSize) {
    // Update animation only if moving
    if (dx != 0 || dy != 0) {
      updateAnimation();
    }

    // Set direction based on movement
    if (dx < 0) direction = "left";
    if (dx > 0) direction = "right";
    if (dy < 0) direction = "up";
    if (dy > 0) direction = "down";

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
   * Checks if a proposed position would result in a collision. Considers wall tiles, placed
   * objects, and monsters.
   *
   * @param newX Proposed x position
   * @param newY Proposed y position
   * @param tileManager Reference to the tile manager
   * @param tileSize Size of each tile in pixels
   * @return true if there would be a collision, false if position is valid
   */
  private boolean checkCollision(int newX, int newY, TileManager tileManager, int tileSize) {
    // Calculate collision box dimensions
    int boxWidth = (int) (tileSize * COLLISION_BOX_WIDTH); // 8 pixels
    int boxHeight = (int) (tileSize * COLLISION_BOX_HEIGHT); // 6 pixels

    // Calculate offsets to position the box at the bottom-center of the sprite
    int xOffset = (int) (tileSize * COLLISION_BOX_X_OFFSET); // 4 pixels from left
    int yOffset = (int) (tileSize * COLLISION_BOX_Y_OFFSET); // 10 pixels from top

    // Calculate collision box position
    int collisionX = newX + xOffset;
    int collisionY = newY + yOffset;

    // Check wall collisions with smaller box
    if (tileManager.checkTileCollision(collisionX, collisionY, boxWidth, boxHeight)) {
      return true;
    }

    // Convert collision box coordinates to grid positions
    int leftTile = collisionX / tileSize;
    int rightTile = (collisionX + boxWidth - 1) / tileSize;
    int topTile = collisionY / tileSize;
    int bottomTile = (collisionY + boxHeight - 1) / tileSize;

    // Check object collisions for all tiles the collision box might overlap
    for (GameState.PlacedObject obj : gameState.getPlacedObjects()) {
      if ((obj.gridX >= leftTile && obj.gridX <= rightTile)
          && (obj.gridY >= topTile && obj.gridY <= bottomTile)) {
        return true;
      }
    }

    // Check monster collisions with smaller collision box
    for (Monster monster : gameState.getMonsters()) {
      int monsterGridX = monster.getX() / tileSize;
      int monsterGridY = monster.getY() / tileSize;

      if ((monsterGridX >= leftTile && monsterGridX <= rightTile)
          && (monsterGridY >= topTile && monsterGridY <= bottomTile)) {
        return true;
      }
    }

    return false;
  }

  /** Loads the hero's sprite image from resources. */
  private void loadImage() {
    try {
      image = ImageIO.read(getClass().getResourceAsStream("/hero/player.png"));
      left1 = ImageIO.read(getClass().getResourceAsStream("/hero/player_left1.png"));
      left2 = ImageIO.read(getClass().getResourceAsStream("/hero/player_left2.png"));
      right1 = ImageIO.read(getClass().getResourceAsStream("/hero/player_right1.png"));
      right2 = ImageIO.read(getClass().getResourceAsStream("/hero/player_right2.png"));
      up1 = ImageIO.read(getClass().getResourceAsStream("/hero/player_up1.png"));
      up2 = ImageIO.read(getClass().getResourceAsStream("/hero/player_up2.png"));
      down1 = ImageIO.read(getClass().getResourceAsStream("/hero/player_down1.png"));
      down2 = ImageIO.read(getClass().getResourceAsStream("/hero/player_down2.png"));
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

  /**
   * Gets the current health of the hero.
   *
   * @return Current health value
   */
  public int getHealth() {
    return health;
  }

  /**
   * Gets the maximum possible health of the hero.
   *
   * @return Maximum health value
   */
  public int getMaxHealth() {
    return MAX_HEALTH;
  }

  /** Increases the hero's health by 1, up to the maximum. */
  public void gainHealth() {
    if (health < MAX_HEALTH) {
      health++;
    }
  }

  /**
   * Decreases the hero's health by 1 and triggers damage effect. Won't take damage from archers if
   * cloaked.
   */
  public void loseHealth(Monster attacker) {
    // If attacker is an archer and hero is cloaked, ignore the damage
    if (attacker != null
        && attacker.getType() == Monster.Type.ARCHER
        && gameState.isCloakEffectActive()) {
      return;
    }

    if (health > 0) {
      health--;
      isDamaged = true;
      damageEffectStartTime = System.currentTimeMillis();
    }
  }

  /**
   * Resets the spawn position flag to allow setting a new spawn position. Used when transitioning
   * between halls.
   */
  public void resetSpawnPosition() {
    this.spawnPositionSet = false;
  }

  /** Updates the hero's animation state. Should be called every game update. */
  public void updateAnimation() {
    spriteCounter++;
    if (spriteCounter > SPRITE_CHANGE_RATE) {
      spriteNum = (spriteNum == 1) ? 2 : 1;
      spriteCounter = 0;
    }
  }

  /**
   * Gets the current sprite image based on direction and animation state. Also applies damage
   * effect if the hero is currently damaged.
   *
   * @return The BufferedImage to display
   */
  public BufferedImage getCurrentSprite() {
    BufferedImage currentSprite =
        switch (direction) {
          case "left" -> (spriteNum == 1) ? left1 : left2;
          case "right" -> (spriteNum == 1) ? right1 : right2;
          case "up" -> (spriteNum == 1) ? up1 : up2;
          case "down" -> (spriteNum == 1) ? down1 : down2;
          default -> image;
        };

    // Apply cloak effect if active
    if (gameState.isCloakEffectActive()) {
      // Get remaining time of cloak effect
      long remainingTime = gameState.getCloakRemainingTime();

      // For last 3 seconds, blink between normal and shadow
      if (remainingTime <= 3000) {
        // Blink every 250ms
        if ((System.currentTimeMillis() % 500) < 250) {
          return currentSprite; // Show normal sprite
        }
      }

      // Create shadowy version of sprite
      BufferedImage shadowSprite =
          new BufferedImage(
              currentSprite.getWidth(), currentSprite.getHeight(), BufferedImage.TYPE_INT_ARGB);

      // Make sprite semi-transparent and bluish
      for (int x = 0; x < currentSprite.getWidth(); x++) {
        for (int y = 0; y < currentSprite.getHeight(); y++) {
          int rgb = currentSprite.getRGB(x, y);
          if ((rgb >> 24) != 0) { // If pixel is not transparent
            // Add blue tint and make semi-transparent
            int alpha = 128; // 50% transparency
            int r = ((rgb >> 16) & 0xFF) / 2;
            int g = ((rgb >> 8) & 0xFF) / 2;
            int b = Math.min(255, ((rgb & 0xFF) + 50)); // Add blue tint
            shadowSprite.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
          }
        }
      }
      return shadowSprite;
    }

    // Apply damage effect if active
    if (isDamaged) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - damageEffectStartTime > DAMAGE_EFFECT_DURATION) {
        isDamaged = false;
      } else {
        // Create a red-tinted copy of the sprite
        BufferedImage tintedSprite =
            new BufferedImage(
                currentSprite.getWidth(), currentSprite.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < currentSprite.getWidth(); x++) {
          for (int y = 0; y < currentSprite.getHeight(); y++) {
            int rgb = currentSprite.getRGB(x, y);
            if ((rgb >> 24) != 0) { // If pixel is not transparent
              // Increase red component while reducing green and blue
              int alpha = (rgb >> 24) & 0xff;
              int red = Math.min(255, ((rgb >> 16) & 0xff) + 100);
              int green = Math.max(0, ((rgb >> 8) & 0xff) - 50);
              int blue = Math.max(0, (rgb & 0xff) - 50);
              rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
              tintedSprite.setRGB(x, y, rgb);
            }
          }
        }
        return tintedSprite;
      }
    }

    return currentSprite;
  }

  /**
   * Gets the collision box bounds for debugging or UI purposes.
   *
   * @return int array containing [x, y, width, height] of collision box
   */
  public int[] getCollisionBox(int tileSize) {
    int boxWidth = (int) (tileSize * COLLISION_BOX_WIDTH);
    int boxHeight = (int) (tileSize * COLLISION_BOX_HEIGHT);
    int xOffset = (int) (tileSize * COLLISION_BOX_X_OFFSET);
    int yOffset = (int) (tileSize * COLLISION_BOX_Y_OFFSET);

    return new int[] {x + xOffset, y + yOffset, boxWidth, boxHeight};
  }
}
