/**
 * Represents the player character in the game. Handles hero movement, collision detection, and
 * sprite management. Extends the base Entity class with hero-specific functionality.
 */
package domain.model.entity;

import domain.model.GameState;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import ui.tile.TileManager;

public class Hero extends Entity {
  private static final long serialVersionUID = 1L;

  /** The hero's sprite image */
  private transient BufferedImage image;
  private transient BufferedImage left1, left2;
  private transient BufferedImage right1, right2;
  private transient BufferedImage up1, up2;
  private transient BufferedImage down1, down2;

  private String direction;

  /** Tracks if the initial spawn position has been set */
  private boolean spawnPositionSet = false;

  /** Default movement speed of the hero */
  private static final int DEFAULT_SPEED = 4;

  /** Reference to the game state for collision detection */
  private transient GameState gameState;

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
   * Sets the initial default values for the hero. Initializes position, speed,
   * direction, and
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
   * Sets the hero's spawn position to a random valid location. Ensures the spawn
   * point is within
   * the game area and not colliding with objects.
   *
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize    Size of each tile in pixels
   */
  public void setSpawnPosition(TileManager tileManager, int tileSize) {
    if (spawnPositionSet) return;

    // Get game area boundaries from GameState
    int minX = gameState.getGameAreaStart();
    int maxX = gameState.getGameAreaEnd();
    int minY = gameState.getGameAreaStart();
    int maxY = gameState.getGameAreaEnd();

    // If position is already within valid bounds, keep it
    int currentGridX = x / tileSize;
    int currentGridY = y / tileSize;
    if (currentGridX >= minX && currentGridX <= maxX && 
        currentGridY >= minY && currentGridY <= maxY &&
        !tileManager.tile[tileManager.mapTileNum[currentGridX][currentGridY]].collision &&
        !gameState.isTileOccupied(currentGridX, currentGridY)) {
        spawnPositionSet = true;
        return;
    }

    // Otherwise find new valid position
    Random random = new Random();
    while (!spawnPositionSet) {
        int gridX = minX + random.nextInt(maxX - minX + 1);
        int gridY = minY + random.nextInt(maxY - minY + 1);

        if (!tileManager.tile[tileManager.mapTileNum[gridX][gridY]].collision &&
            !gameState.isTileOccupied(gridX, gridY)) {
            x = gridX * tileSize;
            y = gridY * tileSize;
            spawnPositionSet = true;
        }
    }
  }

  /**
   * Requires:
   * - dx and dy are valid movement deltas (typically between -speed and +speed)
   * - tileManager is properly initialized with valid tile mappings
   * - tileSize is positive and matches the game's tile dimensions
   * - hero's current position (x,y) is within valid map bounds
   * 
   * Modifies:
   * - hero's x and y coordinates
   * - hero's direction
   * - hero's animation state (spriteCounter and spriteNum)
   * 
   * Effects:
   * - Updates hero's movement direction based on dx/dy
   * - If movement is collision-free:
   * - Updates hero's position by dx and dy
   * - Updates animation state if moving
   * - If collision would occur:
   * - Maintains current position
   * - Still updates direction and animation
   */

  /**
   * Attempts to move the hero by the specified amount. Checks for collisions
   * before allowing
   * movement. Updates animation and direction.
   *
   * @param dx          Change in x position
   * @param dy          Change in y position
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize    Size of each tile in pixels
   */
  public void moveIfPossible(int dx, int dy, TileManager tileManager, int tileSize) {
    // Update animation only if moving
    if (dx != 0 || dy != 0) {
      updateAnimation();
    }

    // Set direction based on movement
    if (dx < 0)
      direction = "left";
    if (dx > 0)
      direction = "right";
    if (dy < 0)
      direction = "up";
    if (dy > 0)
      direction = "down";

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
   * Requires:
   * - newX and newY are valid pixel coordinates within the game map bounds.
   * - tileManager is a properly initialized instance of TileManager.
   * - tileSize is a positive integer representing the size of a tile in pixels.
   * - gameState is a valid instance containing placed objects and monsters.
   * 
   * Modifies:
   * - None.
   * 
   * Effects:
   * - Calculates a collision box based on the provided newX, newY coordinates.
   * - Checks for collisions with:
   * 1. Wall tiles in the game map using tileManager.
   * 2. Placed objects from the gameState within the collision box area.
   * 3. Monsters from the gameState within the collision box area.
   * - Returns true if any collision is detected with a wall, placed object, or
   * monster.
   * - Returns false if the path is clear of all obstacles.
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
  public void loadImage() {
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
   * Decreases the hero's health by 1 and triggers damage effect. Won't take
   * damage from archers if
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
   * Resets the spawn position flag to allow setting a new spawn position. Used
   * when transitioning
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
   * Gets the current sprite image based on direction and animation state. Also
   * applies damage
   * effect if the hero is currently damaged.
   *
   * @return The BufferedImage to display
   */
  public BufferedImage getCurrentSprite() {
    BufferedImage currentSprite = switch (direction) {
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
      BufferedImage shadowSprite = new BufferedImage(
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
        BufferedImage tintedSprite = new BufferedImage(
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

    return new int[] { x + xOffset, y + yOffset, boxWidth, boxHeight };
  }

  /**
   * Sets the GameState reference. Should be called after deserialization.
   *
   * @param gameState The GameState instance to associate with the Hero
   */
  public void setGameState(GameState gameState) {
    this.gameState = gameState;
  }

  public boolean isSpawnPositionSet() {
    return spawnPositionSet;
  }

  // Add serialization for position
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    // Save the hero's position and state
    out.writeInt(x);
    out.writeInt(y);
    out.writeInt(speed);
    out.writeBoolean(spawnPositionSet);
    out.writeObject(direction);
    out.writeInt(health);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    // Restore the hero's position and state
    x = in.readInt();
    y = in.readInt();
    speed = in.readInt();
    spawnPositionSet = in.readBoolean();
    direction = (String) in.readObject();
    health = in.readInt();
    // Load images after deserialization
    loadImage();
  }
}
