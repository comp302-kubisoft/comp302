/**
 * Overview: This class represents the core model of the game. It keeps track of the hero, monsters,
 * placed objects in each hall, the current hall index, as well as game-related flags (e.g., luring
 * gems, runes found). Essentially, it encapsulates all essential data and coordinates about the
 * game world.
 *
 * <p>Abstract Function: Let H = the hero entity Let M = the set of all monsters Let O = the lists
 * of placed objects by hall (each hall has a sub-list) Let currentHall = an integer denoting which
 * hall is currently active Let runesFound = how many runes have been discovered so far ... (and so
 * on for other relevant fields)
 *
 * <p>AF(GameState) = A conceptual mapping to: ( hero = H, monsters = M, hallObjects = O,
 * currentHall = currentHall, runesFound = runesFound, ... )
 *
 * <p>Representation Invariant: - 0 ≤ currentHall < TOTAL_HALLS - hallObjects has length =
 * TOTAL_HALLS - runesFound ≥ 0 and runesFound ≤ TOTAL_HALLS - No two objects in the same hall
 * occupy the exact same grid position
 */
package domain.model;

import domain.model.entity.Enchantment;
import domain.model.entity.Hero;
import domain.model.entity.Monster;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import ui.sound.SoundManager;
import ui.tile.TileManager;

public class GameState implements Serializable {
  private static final long serialVersionUID = 1L;

  /** The player character entity */
  private Hero hero;

  /** Size of each tile in pixels */
  private int tileSize;

  /** Manages the game's tile-based map */
  private transient TileManager tileManager;

  /** List of all objects placed in each hall */
  private List<List<PlacedObject>> hallObjects;

  /** Current hall being built (0-3) */
  private int currentHall;

  /** Total number of halls */
  public static final int TOTAL_HALLS = 4;

  /** List of all monsters in the game */
  private List<Monster> monsters;

  /** Number of runes found so far */
  private int runesFound = 0;

  /** Whether a rune has been found in the current hall */
  private boolean runeFoundInCurrentHall = false;

  /** Fixed position of the transition tile */
  private static final int TRANSITION_TILE_X = 9;

  private static final int TRANSITION_TILE_Y = 16;

  /** Starting coordinate of the game area (inclusive) */
  private static final int GAME_AREA_START = 2;

  /** Ending coordinate of the game area (inclusive) */
  private static final int GAME_AREA_END = 17;

  /** Maximum number of monsters allowed in play mode */
  private static final int MAX_MONSTERS = 5;

  private transient SoundManager soundManager;

  /**
   * Represents an object placed in the game world. Contains both pixel coordinates and grid
   * positions for precise placement and collision detection.
   */
  public static class PlacedObject implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The type identifier of the placed object */
    public final int type;

    /** Pixel X coordinate in the game world */
    public final int x;

    /** Pixel Y coordinate in the game world */
    public final int y;

    /** Grid X coordinate in the tile system */
    public final int gridX;

    /** Grid Y coordinate in the tile system */
    public final int gridY;

    /** Whether this object has a rune */
    public boolean hasRune;

    /** Enchantment associated with this placed object */
    public Enchantment enchantment;

    /**
     * Creates a new placed object with specified position and type.
     *
     * @param type The type identifier of the object
     * @param x Pixel X coordinate
     * @param y Pixel Y coordinate
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     */
    public PlacedObject(int type, int x, int y, int gridX, int gridY) {
      this.type = type;
      this.x = x;
      this.y = y;
      this.gridX = gridX;
      this.gridY = gridY;
      this.hasRune = false;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeBoolean(hasRune);
      out.writeObject(enchantment);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      hasRune = in.readBoolean();
      enchantment = (Enchantment) in.readObject();
    }
  }

  /** Time limit for each hall in milliseconds */
  private long[] hallTimeLimits;

  /** Time remaining for current hall in milliseconds */
  private long timeRemaining;

  /** Total time limit for the game in milliseconds */
  private long totalTimeLimit;

  public long getTotalTimeLimit() {
    return totalTimeLimit;
  }

  /** Last time update timestamp */
  private long lastUpdateTime;

  /** Whether timer is active */
  private boolean timerActive = false;

  /** List of active enchantments */
  private List<Enchantment> enchantments;

  /** Time of last enchantment spawn */
  private long lastEnchantmentSpawnTime;

  /** Interval between enchantment spawns in milliseconds */
  private static final long ENCHANTMENT_SPAWN_INTERVAL = 12000; // 12 seconds

  /** Time accumulated during pauses */
  private long pauseDuration = 0;

  /** Inventory counts for storable enchantments */
  private final java.util.Map<Enchantment.Type, Integer> enchantmentInventory;

  /** Types of enchantments that can be stored */
  private static final Set<Enchantment.Type> STORABLE_ENCHANTMENTS =
      Set.of(
          Enchantment.Type.REVEAL,
          Enchantment.Type.CLOAK_OF_PROTECTION,
          Enchantment.Type.LURING_GEM);

  private transient BufferedImage luringGemImage;
  private float gemThrowProgress = 0f; // 0 to 1, for throw animation
  private int gemStartX, gemStartY; // Starting position for throw animation
  private int gemTargetX, gemTargetY; // Target position for throw animation
  private static final float GEM_THROW_SPEED = 0.1f; // Adjust for faster/slower throws

  private int maxScreenCol; // Add these fields to store screen dimensions
  private int maxScreenRow;

  /** Initializes a new game state with specified dimensions. */
  public GameState(int tileSize, int maxScreenCol, int maxScreenRow) {
    this.tileSize = tileSize;
    this.maxScreenCol = maxScreenCol;
    this.maxScreenRow = maxScreenRow;
    this.tileManager = new TileManager(tileSize, maxScreenCol, maxScreenRow);
    this.hallObjects = new ArrayList<>();
    for (int i = 0; i < TOTAL_HALLS; i++) {
      this.hallObjects.add(new ArrayList<>());
    }
    this.currentHall = 0;
    this.monsters = new ArrayList<>();
    this.hero = new Hero(this);
    this.soundManager = SoundManager.getInstance();
    this.hallTimeLimits = new long[TOTAL_HALLS];
    this.timeRemaining = 0;
    this.lastUpdateTime = System.currentTimeMillis();
    this.enchantments = new ArrayList<>();
    this.lastEnchantmentSpawnTime = System.currentTimeMillis();
    this.enchantmentInventory = new HashMap<>();
    // Initialize inventory counts to 0
    for (Enchantment.Type type : STORABLE_ENCHANTMENTS) {
      enchantmentInventory.put(type, 0);
    }
    loadLuringGemImage();
  }

  /**
   * Adds a monster to the game world if the maximum limit hasn't been reached.
   *
   * @param monster The monster to add
   * @return true if monster was added, false if at max capacity
   */
  public boolean addMonster(Monster monster) {
    if (monsters.size() >= MAX_MONSTERS) {
      return false;
    }
    monster.setGameState(this);
    monsters.add(monster);
    return true;
  }

  /**
   * Finds a random empty position in the game area.
   *
   * @return int array with [x, y] coordinates, or null if no empty position found
   */
  public int[] findRandomEmptyPosition() {
    List<int[]> emptyPositions = new ArrayList<>();
    Random random = new Random();

    // Collect all empty positions
    for (int x = GAME_AREA_START; x <= GAME_AREA_END; x++) {
      for (int y = GAME_AREA_START; y <= GAME_AREA_END; y++) {
        if (isPositionEmpty(x, y)) {
          emptyPositions.add(
              new int[] {x * tileManager.getTileSize(), y * tileManager.getTileSize()});
        }
      }
    }

    if (emptyPositions.isEmpty()) return null;
    return emptyPositions.get(random.nextInt(emptyPositions.size()));
  }

  /** Checks if a position is empty (no hero, objects, or monsters). */
  private boolean isPositionEmpty(int gridX, int gridY) {
    // Check for hero
    int heroGridX = hero.getX() / tileSize;
    int heroGridY = hero.getY() / tileSize;
    if (heroGridX == gridX && heroGridY == gridY) return false;

    // Check for placed objects
    if (isTileOccupied(gridX, gridY)) return false;

    // Check for monsters
    for (Monster monster : monsters) {
      int monsterGridX = monster.getX() / tileSize;
      int monsterGridY = monster.getY() / tileSize;
      if (monsterGridX == gridX && monsterGridY == gridY) return false;
    }

    // Check for wall tiles
    return !tileManager.tile[tileManager.mapTileNum[gridX][gridY]].collision;
  }

  /**
   * Calculates the Manhattan distance between two grid positions.
   *
   * @param x1 First position's x coordinate
   * @param y1 First position's y coordinate
   * @param x2 Second position's x coordinate
   * @param y2 Second position's y coordinate
   * @return The Manhattan distance between the two positions
   */
  private int calculateDistance(int x1, int y1, int x2, int y2) {
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  /**
   * Updates monster states and handles their interactions with the hero. Should be called each game
   * update.
   */
  public void updateMonsters() {
    int heroGridX = hero.getX() / tileSize;
    int heroGridY = hero.getY() / tileSize;

    // Create a copy of the list to avoid concurrent modification
    List<Monster> monstersToRemove = new ArrayList<>();

    synchronized (monsters) {
      for (Monster monster : monsters) {
        monster.update(tileManager, tileSize);
        if (monster.shouldRemove()) {
          monstersToRemove.add(monster);
        }
      }
      // Remove monsters after iteration
      monsters.removeAll(monstersToRemove);
    }

    // Handle archer attacks
    for (Monster monster : monsters) {
      if (monster.getType() == Monster.Type.ARCHER && monster.canAttack()) {
        int monsterGridX = monster.getX() / tileSize;
        int monsterGridY = monster.getY() / tileSize;

        int distance = calculateDistance(heroGridX, heroGridY, monsterGridX, monsterGridY);

        if (distance <= monster.getAttackRange()) {
          hero.loseHealth(monster);
          monster.setAttackCooldown();
        }
      }
    }
  }

  /**
   * Checks if a given grid position is within the valid game area. The game area is defined by
   * GAME_AREA_START and GAME_AREA_END constants.
   *
   * @param gridX X coordinate to check
   * @param gridY Y coordinate to check
   * @return true if the position is within the game area, false otherwise
   */
  public boolean isWithinGameArea(int gridX, int gridY) {
    return gridX >= GAME_AREA_START
        && gridX <= GAME_AREA_END
        && gridY >= GAME_AREA_START
        && gridY <= GAME_AREA_END;
  }

  /**
   * Checks if a tile at the given grid position is occupied by any placed object in the current
   * hall.
   */
  public boolean isTileOccupied(int gridX, int gridY) {
    for (PlacedObject obj : hallObjects.get(currentHall)) {
      if (obj.gridX == gridX && obj.gridY == gridY) {
        return true;
      }
    }
    return false;
  }

  /**
   * Attempts to add a new object to the current hall. The object will only be placed if the
   * position is within the game area and the target tile is not already occupied.
   */
  public void addPlacedObject(int type, int x, int y, int gridX, int gridY) {
    if (isWithinGameArea(gridX, gridY) && !isTileOccupied(gridX, gridY)) {
      hallObjects.get(currentHall).add(new PlacedObject(type, x, y, gridX, gridY));
    }
  }

  /**
   * Removes a placed object at the specified grid position in the current hall.
   *
   * @param gridX The grid X coordinate of the object to remove
   * @param gridY The grid Y coordinate of the object to remove
   */
  public void removePlacedObject(int gridX, int gridY) {
    List<PlacedObject> currentHallObjects = hallObjects.get(currentHall);
    currentHallObjects.removeIf(obj -> obj.gridX == gridX && obj.gridY == gridY);
  }

  /** Gets the list of placed objects in the current hall. */
  public List<PlacedObject> getPlacedObjects() {
    return hallObjects.get(currentHall);
  }

  /**
   * Gets the hero entity.
   *
   * @return The Hero instance representing the player
   */
  public Hero getHero() {
    return hero;
  }

  /**
   * Gets the tile manager instance.
   *
   * @return The TileManager handling the game's tile system
   */
  public TileManager getTileManager() {
    return tileManager;
  }

  /**
   * Gets the starting coordinate of the game area.
   *
   * @return The minimum valid coordinate for the game area
   */
  public int getGameAreaStart() {
    return GAME_AREA_START;
  }

  /**
   * Gets the ending coordinate of the game area.
   *
   * @return The maximum valid coordinate for the game area
   */
  public int getGameAreaEnd() {
    return GAME_AREA_END;
  }

  /**
   * Assigns a rune to a random placed object in the current hall. Should be called when
   * transitioning from build to play mode.
   */
  public void assignRandomRune() {
    List<PlacedObject> currentHallObjects = hallObjects.get(currentHall);
    if (currentHallObjects.isEmpty()) {
      return;
    }

    // Clear any existing runes
    for (PlacedObject obj : currentHallObjects) {
      obj.hasRune = false;
    }

    // Select a random object to have the rune
    int randomIndex = new java.util.Random().nextInt(currentHallObjects.size());
    PlacedObject selectedObject = currentHallObjects.get(randomIndex);
    selectedObject.hasRune = true;
  }

  /**
   * Checks if the hero is adjacent to a given grid position. Adjacent means the hero is in a tile
   * directly left, right, up, or down.
   *
   * @param gridX X coordinate to check
   * @param gridY Y coordinate to check
   * @param tileSize Size of each tile in pixels
   * @return true if hero is adjacent, false otherwise
   */
  public boolean isHeroAdjacent(int gridX, int gridY, int tileSize) {
    // Get hero's grid position using the same tileSize for both coordinates
    int heroGridX = hero.getX() / tileSize;
    int heroGridY = hero.getY() / tileManager.getTileSize();

    // Check if hero is in any adjacent tile
    return (Math.abs(heroGridX - gridX) == 1 && heroGridY == gridY)
        || // Left or right
        (Math.abs(heroGridY - gridY) == 1 && heroGridX == gridX)
        || (Math.abs(heroGridX - gridX) == 1 && Math.abs(heroGridY - gridY) == 1);
  }

  /**
   * Checks if the hero is on the transition tile. Only relevant if a rune has been found in the
   * current hall.
   *
   * @return true if the hero is on the transition tile and a rune has been found
   */
  public boolean isHeroOnTransitionTile() {
    if (!runeFoundInCurrentHall) {
      return false;
    }

    int heroGridX = hero.getX() / tileManager.getTileSize();
    int heroGridY = hero.getY() / tileManager.getTileSize();

    return heroGridX == TRANSITION_TILE_X && heroGridY == TRANSITION_TILE_Y;
  }

  /**
   * Handles the transition to the next hall or victory. Should be called when the hero is on the
   * transition tile.
   *
   * @return true if this triggered a victory condition
   */
  public boolean handleHallTransition() {
    if (!runeFoundInCurrentHall || !isHeroOnTransitionTile()) {
      return false;
    }

    if (currentHall < TOTAL_HALLS - 1) {
      currentHall++;

      // Change music if entering the final hall during gameplay
      if (currentHall == TOTAL_HALLS - 1) {
        soundManager.stopMusic();
        soundManager.playMusic(5); // Play final hall music
      }

      // Reset and set new spawn position
      hero.resetSpawnPosition();
      hero.setSpawnPosition(tileManager, tileManager.getTileSize());

      // Clear monsters and reset rune flag
      monsters.clear();
      runeFoundInCurrentHall = false;

      // Reset the transition tile back to closed door
      tileManager.mapTileNum[TRANSITION_TILE_X][TRANSITION_TILE_Y + 2] = 2;

      return false;
    } else {
      // Play victory sound and stop background music
      soundManager.stopMusic();
      soundManager.playSFX(4);
      return true; // Victory condition
    }
  }

  /**
   * Checks if an object at the given position has a rune. Returns true and prints a message if a
   * rune is found.
   *
   * @param gridX X coordinate to check
   * @param gridY Y coordinate to check
   * @return true if a rune was found, false otherwise
   *     <p>Requires: - gridX and gridY must be valid grid coordinates within the game area -
   *     currentHall must be initialized and valid (0 to TOTAL_HALLS-1) - hallObjects must be
   *     initialized for the current hall
   *     <p>Modifies: - runesFound (increments if rune is found) - runeFoundInCurrentHall (set to
   *     true if rune is found) - tileManager.mapTileNum[9][18] (changes to 3 if rune is found)
   *     <p>Effects: - Returns true if a rune is found at the specified position - Returns false if
   *     no rune is found or no object exists at position - Plays a sound effect (SFX 1) if rune is
   *     found - Opens the door (changes tile) if rune is found
   */
  public boolean checkForRune(int gridX, int gridY) {
    for (PlacedObject obj : hallObjects.get(currentHall)) {
      if (obj.gridX == gridX && obj.gridY == gridY) {
        if (obj.hasRune) {
          runesFound++;
          runeFoundInCurrentHall = true;

          // Change the tile at row 18, column 9 to type 3 (open door)
          tileManager.mapTileNum[9][18] = 3;

          // Play door unlock sound effect
          soundManager.playSFX(1);

          return true;
        }
        return false;
      }
    }
    return false;
  }

  /** Gets the list of all monsters. */
  public List<Monster> getMonsters() {
    return monsters;
  }

  /** Gets the current hall number (0-3). */
  public int getCurrentHall() {
    return currentHall;
  }

  /**
   * Moves to the next hall. Returns true if there are more halls, false if we've reached the end.
   */
  public boolean moveToNextHall() {
    if (currentHall < TOTAL_HALLS - 1) {
      currentHall++;
      return true;
    }
    return false;
  }

  /** Gets the list of placed objects in a specific hall. */
  public List<PlacedObject> getPlacedObjectsInHall(int hall) {
    return hallObjects.get(hall);
  }

  /** Sets the current hall number (0-3). */
  public void setCurrentHall(int hall) {
    this.currentHall = hall;
  }

  /** Gets the number of runes found so far. */
  public int getRunesFound() {
    return runesFound;
  }

  /** Resets the number of runes found to zero. Called when resetting the game state. */
  public void resetRunesFound() {
    runesFound = 0;
    runeFoundInCurrentHall = false;
  }

  /**
   * Updates the pause duration for all monsters. Should be called when the game is unpaused.
   *
   * @param pauseDuration The duration to add to each monster's pause time
   */
  public void updateMonstersPauseDuration(long pauseDuration) {
    for (Monster monster : monsters) {
      monster.addPauseDuration(pauseDuration);
    }
  }

  /**
   * Updates the timer if active. Should be called each game update.
   *
   * @return true if time has run out, false otherwise
   */
  public boolean updateTimer() {
    if (!timerActive) return false;

    long currentTime = System.currentTimeMillis();
    long delta = currentTime - lastUpdateTime;
    lastUpdateTime = currentTime;

    timeRemaining -= delta;
    return timeRemaining <= 0;
  }

  /** Starts the timer for the current hall. */
  public void startTimer() {
    timerActive = true;
    timeRemaining = hallTimeLimits[currentHall];
    lastUpdateTime = System.currentTimeMillis();
  }

  /** Pauses the timer. */
  public void pauseTimer() {
    if (timerActive) {
      timerActive = false;
      updateTimer(); // Update one last time before pausing
      // Pause all enchantments
      for (Enchantment enchantment : enchantments) {
        enchantment.setPaused(true);
      }
    }
  }

  /** Resumes the timer. */
  public void resumeTimer() {
    if (!timerActive) {
      timerActive = true;
      lastUpdateTime = System.currentTimeMillis();
      // Unpause all enchantments
      for (Enchantment enchantment : enchantments) {
        enchantment.setPaused(false);
      }
    }
  }

  /**
   * Sets the time limit for a specific hall based on number of objects.
   *
   * @param hall The hall number
   */
  public void setHallTimeLimit(int hall) {
    if (hall >= 0 && hall < TOTAL_HALLS) {
      int objectCount = hallObjects.get(hall).size();
      long timeLimit = objectCount * 5L * 1000; // Convert to milliseconds
      hallTimeLimits[hall] = timeLimit;
      if (hall == currentHall) {
        totalTimeLimit = timeLimit;
        timeRemaining = timeLimit;
      }
    }
  }

  /** Gets the current time remaining in milliseconds. */
  public long getTimeRemaining() {
    return timeRemaining;
  }

  /** Resets the timer state. */
  public void resetTimer() {
    timerActive = false;
    timeRemaining = hallTimeLimits[currentHall];
    lastUpdateTime = System.currentTimeMillis();
  }

  /**
   * Updates enchantment states, spawning new ones and removing expired ones. Should be called each
   * game update.
   */
  public void updateEnchantments() {
    // Create a new list to store enchantments that should be removed
    List<Enchantment> enchantmentsToRemove = new ArrayList<>();

    // First pass: identify enchantments that have expired
    for (Enchantment enchantment : enchantments) {
      if (enchantment.hasExpired()) {
        enchantmentsToRemove.add(enchantment);
      }
    }

    // Second pass: remove the expired enchantments
    enchantments.removeAll(enchantmentsToRemove);

    // Check if it's time to spawn a new enchantment
    long currentTime = System.currentTimeMillis();
    long effectiveTime = currentTime - pauseDuration;
    long effectiveLastSpawnTime = lastEnchantmentSpawnTime - pauseDuration;

    if (effectiveTime - effectiveLastSpawnTime >= ENCHANTMENT_SPAWN_INTERVAL) {
      spawnRandomEnchantment();
      lastEnchantmentSpawnTime = currentTime;
    }
  }

  /** Spawns a random enchantment at a random empty location. */
  private void spawnRandomEnchantment() {
    // Get a random empty position
    int[] position = findRandomEmptyPosition();
    if (position == null) return;

    // Choose a random enchantment type
    Enchantment.Type[] types = Enchantment.Type.values();
    Enchantment.Type randomType = types[new Random().nextInt(types.length)];

    // Create and add the enchantment
    enchantments.add(new Enchantment(randomType, position[0], position[1]));
  }

  /** Gets the list of active enchantments. */
  public List<Enchantment> getEnchantments() {
    return enchantments;
  }

  /**
   * Checks if an enchantment exists at the given coordinates and collects it.
   *
   * @param x The x coordinate to check
   * @param y The y coordinate to check
   * @return The collected enchantment, or null if none found
   */
  public Enchantment collectEnchantment(int x, int y) {
    int tileSize = tileManager.getTileSize();
    for (Enchantment enchantment : enchantments) {
      // Check if click is within the enchantment's tile
      if (x >= enchantment.getX()
          && x < enchantment.getX() + tileSize
          && y >= enchantment.getY()
          && y < enchantment.getY() + tileSize) {
        enchantments.remove(enchantment);
        return enchantment;
      }
    }
    return null;
  }

  /**
   * Called when the game is unpaused. Updates pause duration for all time-sensitive entities.
   *
   * @param pauseDuration The duration of the pause in milliseconds
   */
  public void updatePauseDuration(long pauseDuration) {
    this.pauseDuration += pauseDuration;
    // Update monsters' pause duration
    for (Monster monster : monsters) {
      monster.addPauseDuration(pauseDuration);
    }
    // Update enchantments' pause duration
    for (Enchantment enchantment : enchantments) {
      enchantment.addPauseDuration(pauseDuration);
    }
    // Adjust spawn timers
    lastEnchantmentSpawnTime += pauseDuration;
    // Adjust reveal effect timer if active
    if (revealEffectActive) {
      revealEffectStartTime += pauseDuration;
    }
    if (cloakEffectActive) {
      cloakEffectStartTime += pauseDuration;
    }
  }

  /**
   * Resets the enchantment spawn timer. Called when transitioning between halls or starting the
   * game.
   */
  public void resetEnchantmentSpawnTimer() {
    lastEnchantmentSpawnTime = System.currentTimeMillis();
    // Clear any existing enchantments
    enchantments.clear();
  }

  /**
   * Handles the collection of an enchantment. Immediate effect enchantments are applied directly,
   * while storable enchantments are added to inventory.
   *
   * @param enchantment The enchantment that was collected
   */
  public void handleEnchantmentCollection(Enchantment enchantment) {
    Enchantment.Type type = enchantment.getType();

    if (type == Enchantment.Type.EXTRA_TIME) {
      // Add 5 seconds (5000 milliseconds) to the current timer
      timeRemaining += 5000;

    } else if (type == Enchantment.Type.EXTRA_LIFE) {
      // Increase hero's health by 1
      hero.gainHealth();
    } else if (STORABLE_ENCHANTMENTS.contains(type)) {
      // Add to inventory
      enchantmentInventory.put(type, enchantmentInventory.get(type) + 1);
    }
  }

  /**
   * Gets the current count of a storable enchantment in inventory.
   *
   * @param type The type of enchantment to check
   * @return The number of that enchantment in inventory
   */
  public int getEnchantmentCount(Enchantment.Type type) {
    return enchantmentInventory.getOrDefault(type, 0);
  }

  /** Resets the enchantment inventory. Called when resetting the game state. */
  public void resetEnchantmentInventory() {
    for (Enchantment.Type type : STORABLE_ENCHANTMENTS) {
      enchantmentInventory.put(type, 0);
    }
  }

  /** Adds a reveal effect to the game. Should be called when a reveal enchantment is used. */
  public void useRevealEnchantment() {
    // Check if we have a reveal enchantment in inventory
    if (enchantmentInventory.getOrDefault(Enchantment.Type.REVEAL, 0) > 0) {
      // Find object with rune
      for (PlacedObject obj : hallObjects.get(currentHall)) {
        if (obj.hasRune) {
          // Calculate reveal area centered around the rune
          revealAreaX = Math.max(GAME_AREA_START, obj.gridX - (REVEAL_AREA_SIZE / 2));
          revealAreaY = Math.max(GAME_AREA_START, obj.gridY - (REVEAL_AREA_SIZE / 2));

          // Ensure reveal area doesn't go out of bounds
          if (revealAreaX + REVEAL_AREA_SIZE > GAME_AREA_END) {
            revealAreaX = GAME_AREA_END - REVEAL_AREA_SIZE;
          }
          if (revealAreaY + REVEAL_AREA_SIZE > GAME_AREA_END) {
            revealAreaY = GAME_AREA_END - REVEAL_AREA_SIZE;
          }

          // Activate reveal effect
          revealEffectActive = true;
          revealEffectStartTime = System.currentTimeMillis() - pauseDuration;

          // Consume one reveal enchantment
          enchantmentInventory.put(
              Enchantment.Type.REVEAL, enchantmentInventory.get(Enchantment.Type.REVEAL) - 1);
          break;
        }
      }
    }
  }

  /**
   * Checks if the reveal effect is active. Should be called during game updates.
   *
   * @return true if the reveal effect is active, false otherwise
   */
  public boolean isRevealEffectActive() {
    return revealEffectActive;
  }

  /**
   * Gets the starting X coordinate of the reveal area. Should be called during game updates.
   *
   * @return The starting X coordinate of the reveal area
   */
  public int getRevealAreaX() {
    return revealAreaX;
  }

  /**
   * Gets the starting Y coordinate of the reveal area. Should be called during game updates.
   *
   * @return The starting Y coordinate of the reveal area
   */
  public int getRevealAreaY() {
    return revealAreaY;
  }

  /**
   * Gets the size of the reveal area. Should be called during game updates.
   *
   * @return The size of the reveal area
   */
  public int getRevealAreaSize() {
    return REVEAL_AREA_SIZE;
  }

  /** Updates the reveal effect. Should be called during game updates. */
  public void updateRevealEffect() {
    if (revealEffectActive) {
      long currentTime = System.currentTimeMillis() - pauseDuration;
      if (currentTime - revealEffectStartTime >= REVEAL_EFFECT_DURATION) {
        revealEffectActive = false;
      }
    }
  }

  /** Adds a cloak effect to the game. Should be called when a cloak enchantment is used. */
  public void useCloakEnchantment() {
    // Check if we have a cloak enchantment in inventory
    if (enchantmentInventory.getOrDefault(Enchantment.Type.CLOAK_OF_PROTECTION, 0) > 0) {
      // Activate cloak effect
      cloakEffectActive = true;
      cloakEffectStartTime = System.currentTimeMillis() - pauseDuration;

      // Consume one cloak enchantment
      enchantmentInventory.put(
          Enchantment.Type.CLOAK_OF_PROTECTION,
          enchantmentInventory.get(Enchantment.Type.CLOAK_OF_PROTECTION) - 1);
    }
  }

  /**
   * Checks if the cloak effect is active. Should be called during game updates.
   *
   * @return true if the cloak effect is active, false otherwise
   */
  public boolean isCloakEffectActive() {
    return cloakEffectActive;
  }

  /** Updates the cloak effect. Should be called during game updates. */
  public void updateCloakEffect() {
    if (cloakEffectActive) {
      long currentTime = System.currentTimeMillis() - pauseDuration;
      if (currentTime - cloakEffectStartTime >= CLOAK_EFFECT_DURATION) {
        cloakEffectActive = false;
      }
    }
  }

  private boolean revealEffectActive = false;
  private long revealEffectStartTime = 0;
  private int revealAreaX = 0;
  private int revealAreaY = 0;
  private static final long REVEAL_EFFECT_DURATION = 10000; // 10 seconds
  private static final int REVEAL_AREA_SIZE = 4; // 4x4 tiles
  private boolean cloakEffectActive = false;
  private long cloakEffectStartTime = 0;
  private static final long CLOAK_EFFECT_DURATION = 20000; // 20 seconds

  /**
   * Gets the remaining time for cloak effect in milliseconds.
   *
   * @return Remaining time in milliseconds, or 0 if not active
   */
  public long getCloakRemainingTime() {
    if (!cloakEffectActive) return 0;
    long currentTime = System.currentTimeMillis() - pauseDuration;
    long remainingTime = CLOAK_EFFECT_DURATION - (currentTime - cloakEffectStartTime);
    return Math.max(0, remainingTime);
  }

  /** Gets the progress of the reveal effect from 0.0 (start) to 1.0 (end) */
  public float getRevealProgress() {
    if (!revealEffectActive) return 1.0f;
    long currentTime = System.currentTimeMillis() - pauseDuration;
    float progress = (float) (currentTime - revealEffectStartTime) / REVEAL_EFFECT_DURATION;
    return Math.min(1.0f, Math.max(0.0f, progress));
  }

  private boolean luringGemActive = false;
  private int gemX = 0;
  private int gemY = 0;
  private long gemStartTime = 0;
  private static final long GEM_EFFECT_DURATION = 10000; // 10 seconds
  private static final int GEM_THROW_DISTANCE = 5;

  private void loadLuringGemImage() {
    try {
      luringGemImage = ImageIO.read(getClass().getResourceAsStream("/enchantments/luring_gem.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Throws a luring gem in the specified direction from the hero's current position. The gem acts
   * as a distraction that attracts monsters to its location for a limited time. The gem will travel
   * a fixed distance (GEM_THROW_DISTANCE) in the specified direction, staying within game
   * boundaries.
   *
   * <p>Requires: - direction must be one of: "up", "down", "left", "right" - hero must exist in the
   * game state - player must have at least one luring gem in inventory to throw
   *
   * <p>Modifies: - enchantmentInventory (reduces luring gem count) - gem position (gemX, gemY) -
   * gem state (luringGemActive, gemStartTime)
   *
   * <p>Effects: - If player has a luring gem: > Calculates target position GEM_THROW_DISTANCE tiles
   * away > Ensures position stays within game boundaries > Activates the gem effect > Removes one
   * gem from inventory - If no gem in inventory: > No changes occur
   */
  public void throwLuringGem(String direction) {
    if (enchantmentInventory.getOrDefault(Enchantment.Type.LURING_GEM, 0) > 0) {
      // Get hero's position as starting point
      int startX = hero.getX() / tileManager.getTileSize();
      int startY = hero.getY() / tileManager.getTileSize();

      // Store actual pixel coordinates for animation
      gemStartX = hero.getX();
      gemStartY = hero.getY();

      // Calculate target position
      int targetX = startX;
      int targetY = startY;

      switch (direction) {
        case "left" -> targetX = startX - GEM_THROW_DISTANCE;
        case "right" -> targetX = startX + GEM_THROW_DISTANCE;
        case "up" -> targetY = startY - GEM_THROW_DISTANCE;
        case "down" -> targetY = startY + GEM_THROW_DISTANCE;
      }

      // Adjust for boundaries and objects
      while ((targetX < GAME_AREA_START
              || targetX > GAME_AREA_END
              || targetY < GAME_AREA_START
              || targetY > GAME_AREA_END
              || isTileOccupied(targetX, targetY))
          && (targetX != startX || targetY != startY)) {

        switch (direction) {
          case "left" -> targetX++;
          case "right" -> targetX--;
          case "up" -> targetY++;
          case "down" -> targetY--;
        }
      }

      // Place gem if valid position found
      if (targetX != startX || targetY != startY) {
        gemX = targetX;
        gemY = targetY;
        gemTargetX = gemX * tileManager.getTileSize();
        gemTargetY = gemY * tileManager.getTileSize();
        luringGemActive = true;
        gemStartTime = System.currentTimeMillis() - pauseDuration;
        gemThrowProgress = 0f;

        // Consume one luring gem
        enchantmentInventory.put(
            Enchantment.Type.LURING_GEM, enchantmentInventory.get(Enchantment.Type.LURING_GEM) - 1);
      }
    }
  }

  public boolean isLuringGemActive() {
    return luringGemActive;
  }

  public int getGemX() {
    return gemX;
  }

  public int getGemY() {
    return gemY;
  }

  public void updateLuringGemEffect() {
    if (luringGemActive) {
      // Update throw animation
      if (gemThrowProgress < 1.0f) {
        gemThrowProgress += GEM_THROW_SPEED;
        if (gemThrowProgress > 1.0f) gemThrowProgress = 1.0f;
      }

      // Check duration
      long currentTime = System.currentTimeMillis() - pauseDuration;
      if (currentTime - gemStartTime >= GEM_EFFECT_DURATION) {
        luringGemActive = false;
      }
    }
  }

  public float getGemThrowProgress() {
    return gemThrowProgress;
  }

  public int getGemStartX() {
    return gemStartX;
  }

  public int getGemStartY() {
    return gemStartY;
  }

  public int getGemTargetX() {
    return gemTargetX;
  }

  public int getGemTargetY() {
    return gemTargetY;
  }

  public BufferedImage getLuringGemImage() {
    return luringGemImage;
  }

  private static final long GEM_FADE_DURATION = 2000; // Last 2 seconds fade out

  public float getGemFadeAlpha() {
    if (!luringGemActive) return 0f;
    long currentTime = System.currentTimeMillis() - pauseDuration;
    long timeLeft = (gemStartTime + GEM_EFFECT_DURATION) - currentTime;

    if (timeLeft <= GEM_FADE_DURATION) {
      return Math.max(0f, timeLeft / (float) GEM_FADE_DURATION);
    }
    return 1.0f;
  }

  public boolean repOk() {
    // 1. currentHall in valid range
    if (currentHall < 0 || currentHall >= TOTAL_HALLS) {
      return false;
    }

    // 2. hallObjects size check
    if (hallObjects.size() != TOTAL_HALLS) {
      return false;
    }
    for (List<PlacedObject> objectsInHall : hallObjects) {
      if (objectsInHall == null) {
        return false;
      }
      // 4. check for duplicate grid positions within the same hall
      Set<String> seenGridPositions = new HashSet<>();
      for (PlacedObject obj : objectsInHall) {
        String pos = obj.gridX + "," + obj.gridY;
        if (!seenGridPositions.add(pos)) {
          // Duplicate position
          return false;
        }
      }
    }

    // 3. runesFound in valid range
    if (runesFound < 0 || runesFound > TOTAL_HALLS) {
      return false;
    }

    // 5. Basic monster list checks
    if (monsters == null) {
      return false;
    }
    for (Monster m : monsters) {
      if (m == null) {
        return false;
      }
    }

    // If we got here, the rep is OK.
    return true;
  }

  public void reinitialize(int tileSize, int maxScreenCol, int maxScreenRow) {
    if (tileSize <= 0 || maxScreenCol <= 0 || maxScreenRow <= 0) {
      throw new IllegalArgumentException("Invalid initialization parameters");
    }

    this.tileSize = tileSize;
    this.maxScreenCol = maxScreenCol;
    this.maxScreenRow = maxScreenRow;
    // Only create new TileManager if it doesn't exist
    if (this.tileManager == null) {
      this.tileManager = new TileManager(tileSize, maxScreenCol, maxScreenRow);
    }

    this.soundManager = SoundManager.getInstance();

    // Reinitialize hero
    if (hero != null) {
      hero.loadImage();
      hero.setGameState(this);
    }

    // Reinitialize monsters
    if (monsters != null) {
      for (Monster monster : monsters) {
        monster.loadImage();
        monster.setGameState(this);
        monster.setSoundManager(soundManager);
        // Reset any necessary state
        if (monster.getType() == Monster.Type.WIZARD) {
          monster.initializeWizardBehavior();
        }
        // Ensure monster is in valid position
        if (!isWithinGameArea(monster.getX() / tileSize, monster.getY() / tileSize)) {
          int[] newPos = findRandomEmptyPosition();
          if (newPos != null) {
            monster.setPosition(newPos[0], newPos[1]);
          }
        }
      }
    }

    loadLuringGemImage();
    lastUpdateTime = System.currentTimeMillis();
  }

  public int getTileSize() {
    return tileSize;
  }

  private long gameTime; // Track total game time
  private long lastPauseTime;
  private long totalPauseDuration;

  public void updateGameTime() {
    if (!isPaused) {
      gameTime = System.currentTimeMillis() - startTime - totalPauseDuration;
    }
  }

  private boolean isPaused;
  private long startTime;

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeLong(gameTime);
    out.writeLong(lastPauseTime);
    out.writeLong(totalPauseDuration);
    out.writeInt(maxScreenCol);
    out.writeInt(maxScreenRow);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    gameTime = in.readLong();
    lastPauseTime = in.readLong();
    totalPauseDuration = in.readLong();
    maxScreenCol = in.readInt();
    maxScreenRow = in.readInt();
    reinitialize(tileSize, maxScreenCol, maxScreenRow);
  }

  public void spawnMonster() {
    if (monsters.size() >= MAX_MONSTERS) {
      return;
    }

    int[] position = findRandomEmptyPosition();
    if (position == null) {
      return;
    }

    Random random = new Random();
    // Simply pick a random monster type from all available types
    Monster.Type monsterType = Monster.Type.values()[random.nextInt(Monster.Type.values().length)];

    Monster monster = new Monster(monsterType, position[0], position[1]);
    monster.setGameState(this);
    monsters.add(monster);
  }

  private String saveMessage = null;
  private long saveMessageStartTime = 0;
  private static final long SAVE_MESSAGE_DURATION = 2000; // 2 seconds in milliseconds

  public void setSaveMessage(String message) {
    this.saveMessage = message;
    this.saveMessageStartTime = System.currentTimeMillis();
  }

  public String getSaveMessage() {
    if (saveMessage != null) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - saveMessageStartTime > SAVE_MESSAGE_DURATION) {
        saveMessage = null;
      }
    }
    return saveMessage;
  }
}
