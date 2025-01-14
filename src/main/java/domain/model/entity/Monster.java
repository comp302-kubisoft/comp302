package domain.model.entity;

import domain.model.GameState;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import ui.tile.TileManager;

public class Monster extends Entity implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Type {
    FIGHTER,
    WIZARD,
    ARCHER
  }

  private Type monsterType;
  private transient BufferedImage image;
  private long lastAttackTime = 0;
  private static final long ARCHER_ATTACK_COOLDOWN = 1000; // 1 second in milliseconds
  private static final long FIGHTER_ATTACK_COOLDOWN = 1000; // 1 second in milliseconds
  private static final int ARCHER_ATTACK_RANGE = 4; // 4 tiles range
  private static final int DEFAULT_SPEED = 2; // Half the hero's speed
  private static final long DIRECTION_CHANGE_INTERVAL = 2000; // 2 seconds
  private long lastDirectionChange;
  private Random random = new Random();
  private GameState gameState;
  private static final String[] DIRECTIONS = { "up", "down", "left", "right" };
  private transient ui.sound.SoundManager soundManager;
  private static final long WIZARD_TELEPORT_INTERVAL = 5000; // 5 seconds
  private long lastTeleportTime = 0;
  private boolean isCastingSpell = false;
  private static final long SPELL_EFFECT_DURATION = 500; // 0.5 seconds spell effect
  private long pauseDuration = 0; // Track total pause duration
  private transient TileManager tileManager;
  private transient int tileSize;
  private WizardStrategy wizardStrategy;
  private boolean shouldRemove = false;

  public Monster(Type type, int x, int y) {
    this.monsterType = type;
    this.x = x;
    this.y = y;
    if (type == Type.FIGHTER) {
      this.speed = DEFAULT_SPEED;
      this.direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
      this.lastDirectionChange = System.currentTimeMillis();
    } else if (type == Type.WIZARD) {
      this.lastTeleportTime = System.currentTimeMillis(); // Initialize teleport timer at spawn
    }
    this.soundManager = ui.sound.SoundManager.getInstance();
    loadImage();
  }

  public void loadImage() {
    try {
      String imagePath = switch (monsterType) {
        case FIGHTER -> "/monsters/fighter.png";
        case WIZARD -> "/monsters/wizard.png";
        case ARCHER -> "/monsters/archer.png";
      };
      image = ImageIO.read(getClass().getResourceAsStream(imagePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public BufferedImage getImage() {
    if (monsterType == Type.WIZARD && isCastingSpell) {
      // Create a glowing effect by brightening the image
      BufferedImage glowingImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
      for (int x = 0; x < image.getWidth(); x++) {
        for (int y = 0; y < image.getHeight(); y++) {
          int rgb = image.getRGB(x, y);
          if ((rgb >> 24) != 0) { // If pixel is not transparent
            // Add white tint to create glowing effect
            int r = Math.min(255, ((rgb >> 16) & 0xFF) + 100);
            int g = Math.min(255, ((rgb >> 8) & 0xFF) + 100);
            int b = Math.min(255, (rgb & 0xFF) + 100);
            glowingImage.setRGB(x, y, (rgb & 0xFF000000) | (r << 16) | (g << 8) | b);
          }
        }
      }
      return glowingImage;
    }
    return image;
  }

  public Type getType() {
    return monsterType;
  }

  public void setGameState(GameState gameState) {
    this.gameState = gameState;
  }

  /**
   * Chooses and initializes the wizard's strategy based on the game time
   * remaining.
   * If 'previousStrategy' is different from the newly chosen strategy, we call
   * init().
   *
   * This can be called every update to handle dynamic adaptation.
   */
  private void chooseAndInitStrategy(WizardStrategy previousStrategy) {
    // If not a wizard, do nothing
    if (monsterType != Type.WIZARD || gameState == null)
      return;

    double remainingTime = gameState.getTimeRemaining();
    double totalTime = gameState.getTotalTimeLimit();
    double remainingPercent = (totalTime > 0) ? (remainingTime / totalTime) * 100.0 : 0.0;

    WizardStrategy newStrategy;
    if (remainingPercent < 30.0) {
      newStrategy = new BadSituationWizardStrategy();
    } else if (remainingPercent > 70.0) {
      newStrategy = new GoodSituationWizardStrategy();
    } else {
      newStrategy = new IndecisiveWizardStrategy();
    }

    // Only re-init if we switch to a different strategy
    if (wizardStrategy == null || !wizardStrategy.getClass().equals(newStrategy.getClass())) {
      wizardStrategy = newStrategy;
      wizardStrategy.init(this, gameState);
    }
  }

  /**
   * Returns the current in-game time adjusted for pauses.
   */
  public long getAdjustedTime() {
    return System.currentTimeMillis() - pauseDuration;
  }

  /**
   * Updates the pause duration when game is paused/unpaused.
   *
   * @param pauseTime The duration to add to total pause time
   */
  public void addPauseDuration(long pauseTime) {
    this.pauseDuration += pauseTime;
  }

  /**
   * Updates the monster's position and behavior based on its type. Fighter
   * monsters move randomly,
   * changing direction periodically. Wizard monsters teleport runes periodically.
   *
   * @param tileManager Reference to the tile manager for collision checking
   * @param tileSize    Size of each tile in pixels
   */
  public void update(TileManager tileManager, int tileSize) {
    this.tileManager = tileManager;
    this.tileSize = tileSize;

    if (monsterType == Type.WIZARD) {
      // Decide which strategy to use right now, possibly switching from a prior
      // strategy
      WizardStrategy oldStrategy = this.wizardStrategy;
      chooseAndInitStrategy(oldStrategy);

      // Now delegate to the chosen strategy's update method
      if (wizardStrategy != null) {
        wizardStrategy.update(this, gameState);
      }
      return;
    }

    if (monsterType == Type.ARCHER && canAttack()) {
      Hero hero = gameState.getHero();

      // First check if hero is cloaked - if so, archer can't see or attack at all
      if (!gameState.isCloakEffectActive()) {
        int heroGridX = hero.getX() / tileSize;
        int heroGridY = hero.getY() / tileSize;
        int monsterGridX = x / tileSize;
        int monsterGridY = y / tileSize;

        int distance = Math.abs(heroGridX - monsterGridX) + Math.abs(heroGridY - monsterGridY);

        if (distance <= ARCHER_ATTACK_RANGE) {
          hero.loseHealth(this);
          setAttackCooldown();
          soundManager.playSFX(2);
        }
      }
    } else if (monsterType == Type.WIZARD) {
      // Check if it's time to teleport rune
      long currentTime = getAdjustedTime();
      if (currentTime - lastTeleportTime >= WIZARD_TELEPORT_INTERVAL) {
        teleportRune();
        lastTeleportTime = currentTime;
      }
    }

    // Fighter movement update (only if it's a fighter)
    if (monsterType == Type.FIGHTER) {
      // First check if there's an active gem to follow
      if (gameState.isLuringGemActive()) {
        moveTowardsGem();
      } else {
        // Only do random movement if not following a gem
        // Check if it's time to change direction
        long currentTime = getAdjustedTime();
        if (currentTime - lastDirectionChange >= DIRECTION_CHANGE_INTERVAL) {
          direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
          lastDirectionChange = currentTime;
        }

        // Calculate movement based on direction
        int dx = 0, dy = 0;
        switch (direction) {
          case "up" -> dy = -speed;
          case "down" -> dy = speed;
          case "left" -> dx = -speed;
          case "right" -> dx = speed;
        }

        // Try to move in the calculated direction
        moveIfPossible(dx, dy, tileManager, tileSize);
      }
    }
  }

  /**
   * Attempts to move the monster by the specified amount. Checks for collisions
   * before allowing
   * movement.
   */
  private void moveIfPossible(int dx, int dy, TileManager tileManager, int tileSize) {
    if (dx != 0) {
      int newX = x + dx;
      if (!checkCollision(newX, y, tileManager, tileSize)) {
        x = newX;
      } else {
        // If collision, try a different direction
        direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        lastDirectionChange = System.currentTimeMillis();
      }
    }

    if (dy != 0) {
      int newY = y + dy;
      if (!checkCollision(x, newY, tileManager, tileSize)) {
        y = newY;
      } else {
        // If collision, try a different direction
        direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        lastDirectionChange = System.currentTimeMillis();
      }
    }
  }

  /** Checks if a proposed position would result in a collision. */
  private boolean checkCollision(int newX, int newY, TileManager tileManager, int tileSize) {
    // Check wall collisions
    int collisionBoxSize = (int) (tileSize * 0.8); // 80% of tile size for collision
    int xOffset = (tileSize - collisionBoxSize) / 2;
    int yOffset = (tileSize - collisionBoxSize) / 2;

    // Check wall collisions
    if (tileManager.checkTileCollision(
        newX + xOffset, newY + yOffset, collisionBoxSize, collisionBoxSize)) {
      return true;
    }

    // Convert to grid coordinates
    int gridX = (newX + tileSize / 2) / tileSize;
    int gridY = (newY + tileSize / 2) / tileSize;

    // Check object collisions
    if (gameState != null && gameState.isTileOccupied(gridX, gridY)) {
      return true;
    }

    // Check hero collision
    if (gameState != null) {
      Hero hero = gameState.getHero();
      int heroGridX = (hero.getX() + tileSize / 2) / tileSize;
      int heroGridY = (hero.getY() + tileSize / 2) / tileSize;
      if (gridX == heroGridX && gridY == heroGridY) {
        // Only fighter should damage on collision
        if (monsterType == Type.FIGHTER && canAttack()) {
          hero.loseHealth(this);
          setAttackCooldown();
          soundManager.playSFX(2);
        }
        return true;
      }
    }

    // Check other monster collisions
    if (gameState != null) {
      for (Monster other : gameState.getMonsters()) {
        if (other != this) {
          int otherGridX = (other.getX() + tileSize / 2) / tileSize;
          int otherGridY = (other.getY() + tileSize / 2) / tileSize;
          if (gridX == otherGridX && gridY == otherGridY) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Checks if this monster can attack based on its type and cooldown.
   *
   * @return true if the monster can attack, false otherwise
   */
  public boolean canAttack() {
    long currentTime = getAdjustedTime();
    if (monsterType == Type.ARCHER) {
      return currentTime - lastAttackTime >= ARCHER_ATTACK_COOLDOWN;
    } else if (monsterType == Type.FIGHTER) {
      return currentTime - lastAttackTime >= FIGHTER_ATTACK_COOLDOWN;
    }
    return false;
  }

  /** Marks this monster as having just attacked, starting its cooldown. */
  public void setAttackCooldown() {
    lastAttackTime = getAdjustedTime();
  }

  /**
   * Gets the attack range for this monster.
   *
   * @return The number of tiles this monster can attack from
   */
  public int getAttackRange() {
    return monsterType == Type.ARCHER ? ARCHER_ATTACK_RANGE : 0;
  }

  /**
   * Teleports the rune to a random placed object within the current hall.
   * 
   * Requires:
   * - The game state (`gameState`) must not be null.
   * - There must be at least one placed object in the current hall.
   * 
   * Modifies:
   * - The `hasRune` property of placed objects in the current hall.
   * 
   * Effects:
   * - Transfers the rune from its current holder to a randomly selected
   * placed object in the current hall, excluding the current holder.
   * - If no other placed object exists, the rune remains with its current holder.
   * - Plays a teleport sound effect if successful.
   */
  public void teleportRune() {
    if (gameState == null)
      return;

    List<GameState.PlacedObject> objects = gameState.getPlacedObjects();
    if (objects.isEmpty())
      return;

    // Start spell casting effect
    isCastingSpell = true;
    new Thread(
        () -> {
          try {
            Thread.sleep(SPELL_EFFECT_DURATION);
            isCastingSpell = false;
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        })
        .start();

    // Find current rune holder
    final GameState.PlacedObject currentRuneHolder = objects.stream().filter(obj -> obj.hasRune).findFirst()
        .orElse(null);

    if (currentRuneHolder != null) {
      // Remove rune from current holder
      currentRuneHolder.hasRune = false;

      // Select a random object (excluding the current holder)
      List<GameState.PlacedObject> availableObjects = objects.stream()
          .filter(obj -> obj != currentRuneHolder)
          .collect(java.util.stream.Collectors.toList());

      if (!availableObjects.isEmpty()) {
        // Give rune to random object
        int randomIndex = random.nextInt(availableObjects.size());
        availableObjects.get(randomIndex).hasRune = true;
        // Play teleport sound
        soundManager.playSFX(7);
      } else {
        // If no other objects available, put it back
        currentRuneHolder.hasRune = true;
      }
    }
  }

  private void moveTowardsGem() {
    if (gameState.isLuringGemActive() && monsterType == Type.FIGHTER) {
      int gemX = gameState.getGemX() * tileSize;
      int gemY = gameState.getGemY() * tileSize;

      // Calculate direction to gem
      int dx = 0, dy = 0;

      if (x < gemX)
        dx = speed;
      else if (x > gemX)
        dx = -speed;

      if (y < gemY)
        dy = speed;
      else if (y > gemY)
        dy = -speed;

      // Try to move towards gem
      if (dx != 0) {
        if (!checkCollision(x + dx, y, tileManager, tileSize)) {
          x += dx;
        }
      }
      if (dy != 0) {
        if (!checkCollision(x, y + dy, tileManager, tileSize)) {
          y += dy;
        }
      }
    }
  }

  public boolean shouldRemove() {
    return shouldRemove;
  }

  public void markForRemoval() {
    shouldRemove = true;
  }
}
