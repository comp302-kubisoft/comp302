/**
 * Represents the core game state and manages all game entities and objects.
 * This class serves as the central model maintaining the current state of the game,
 * including the hero, placed objects, and game area boundaries.
 */
package domain.model;

import domain.model.entity.Hero;
import domain.model.entity.Monster;
import domain.model.entity.Enchantment;
import ui.tile.TileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ui.sound.SoundManager;
import java.util.Set;
import java.util.HashMap;

public class GameState {

  /** The player character entity */
  private Hero hero;
  /** Manages the game's tile-based map */
  private TileManager tileManager;
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

  private SoundManager soundManager;

  /**
   * Represents an object placed in the game world.
   * Contains both pixel coordinates and grid positions for precise placement and
   * collision detection.
   */
  public static class PlacedObject {
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

    /**
     * Creates a new placed object with specified position and type.
     * 
     * @param type  The type identifier of the object
     * @param x     Pixel X coordinate
     * @param y     Pixel Y coordinate
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
  }

  /**
   * Time limit for each hall in milliseconds
   */
  private long[] hallTimeLimits;
  /**
   * Time remaining for current hall in milliseconds
   */
  private long timeRemaining;
  /**
   * Last time update timestamp
   */
  private long lastUpdateTime;
  /**
   * Whether timer is active
   */
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
  private static final Set<Enchantment.Type> STORABLE_ENCHANTMENTS = Set.of(
      Enchantment.Type.REVEAL,
      Enchantment.Type.CLOAK_OF_PROTECTION,
      Enchantment.Type.LURING_GEM);

  /**
   * Initializes a new game state with specified dimensions.
   */
  public GameState(int tileSize, int maxScreenCol, int maxScreenRow) {
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
          emptyPositions.add(new int[] { x * tileManager.getTileSize(), y * tileManager.getTileSize() });
        }
      }
    }

    if (emptyPositions.isEmpty())
      return null;
    return emptyPositions.get(random.nextInt(emptyPositions.size()));
  }

  /**
   * Checks if a position is empty (no hero, objects, or monsters).
   */
  private boolean isPositionEmpty(int gridX, int gridY) {
    // Check for hero
    int heroGridX = hero.getX() / tileManager.getTileSize();
    int heroGridY = hero.getY() / tileManager.getTileSize();
    if (heroGridX == gridX && heroGridY == gridY)
      return false;

    // Check for placed objects
    if (isTileOccupied(gridX, gridY))
      return false;

    // Check for monsters
    for (Monster monster : monsters) {
      int monsterGridX = monster.getX() / tileManager.getTileSize();
      int monsterGridY = monster.getY() / tileManager.getTileSize();
      if (monsterGridX == gridX && monsterGridY == gridY)
        return false;
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
   * Updates monster states and handles their interactions with the hero.
   * Should be called each game update.
   */
  public void updateMonsters() {
    int tileSize = tileManager.getTileSize();
    int heroGridX = hero.getX() / tileSize;
    int heroGridY = hero.getY() / tileSize;

    for (Monster monster : monsters) {
      // Update fighter movement
      monster.update(tileManager, tileSize);

      // Handle archer attacks
      if (monster.getType() == Monster.Type.ARCHER && monster.canAttack()) {
        int monsterGridX = monster.getX() / tileSize;
        int monsterGridY = monster.getY() / tileSize;

        int distance = calculateDistance(heroGridX, heroGridY, monsterGridX, monsterGridY);

        if (distance <= monster.getAttackRange()) {
          hero.loseHealth();
          monster.setAttackCooldown();
        }
      }
    }
  }

  /**
   * Checks if a given grid position is within the valid game area.
   * The game area is defined by GAME_AREA_START and GAME_AREA_END constants.
   * 
   * @param gridX X coordinate to check
   * @param gridY Y coordinate to check
   * @return true if the position is within the game area, false otherwise
   */
  public boolean isWithinGameArea(int gridX, int gridY) {
    return gridX >= GAME_AREA_START && gridX <= GAME_AREA_END &&
        gridY >= GAME_AREA_START && gridY <= GAME_AREA_END;
  }

  /**
   * Checks if a tile at the given grid position is occupied by any placed object
   * in the current hall.
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
   * Attempts to add a new object to the current hall.
   * The object will only be placed if the position is within the game area
   * and the target tile is not already occupied.
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

  /**
   * Gets the list of placed objects in the current hall.
   */
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
   * Assigns a rune to a random placed object in the current hall.
   * Should be called when transitioning from build to play mode.
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
   * Checks if the hero is adjacent to a given grid position.
   * Adjacent means the hero is in a tile directly left, right, up, or down.
   * 
   * @param gridX    X coordinate to check
   * @param gridY    Y coordinate to check
   * @param tileSize Size of each tile in pixels
   * @return true if hero is adjacent, false otherwise
   */
  public boolean isHeroAdjacent(int gridX, int gridY, int tileSize) {
    // Get hero's grid position using the same tileSize for both coordinates
    int heroGridX = hero.getX() / tileSize;
    int heroGridY = hero.getY() / tileManager.getTileSize();

    // Check if hero is in any adjacent tile
    return (Math.abs(heroGridX - gridX) == 1 && heroGridY == gridY) || // Left or right
        (Math.abs(heroGridY - gridY) == 1 && heroGridX == gridX)
        || (Math.abs(heroGridX - gridX) == 1 && Math.abs(heroGridY - gridY) == 1);
  }

  /**
   * Checks if the hero is on the transition tile.
   * Only relevant if a rune has been found in the current hall.
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
   * Handles the transition to the next hall or victory.
   * Should be called when the hero is on the transition tile.
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
   * Checks if an object at the given position has a rune.
   * Returns true and prints a message if a rune is found.
   * 
   * @param gridX X coordinate to check
   * @param gridY Y coordinate to check
   * @return true if a rune was found, false otherwise
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

  /**
   * Gets the list of all monsters.
   */
  public List<Monster> getMonsters() {
    return monsters;
  }

  /**
   * Gets the current hall number (0-3).
   */
  public int getCurrentHall() {
    return currentHall;
  }

  /**
   * Moves to the next hall. Returns true if there are more halls, false if we've
   * reached the end.
   */
  public boolean moveToNextHall() {
    if (currentHall < TOTAL_HALLS - 1) {
      currentHall++;
      return true;
    }
    return false;
  }

  /**
   * Gets the list of placed objects in a specific hall.
   */
  public List<PlacedObject> getPlacedObjectsInHall(int hall) {
    return hallObjects.get(hall);
  }

  /**
   * Sets the current hall number (0-3).
   */
  public void setCurrentHall(int hall) {
    if (hall >= 0 && hall < TOTAL_HALLS) {
      this.currentHall = hall;
    }
  }

  /**
   * Gets the number of runes found so far.
   */
  public int getRunesFound() {
    return runesFound;
  }

  /**
   * Resets the number of runes found to zero.
   * Called when resetting the game state.
   */
  public void resetRunesFound() {
    runesFound = 0;
    runeFoundInCurrentHall = false;
  }

  /**
   * Updates the pause duration for all monsters.
   * Should be called when the game is unpaused.
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
    if (!timerActive)
      return false;

    long currentTime = System.currentTimeMillis();
    long delta = currentTime - lastUpdateTime;
    lastUpdateTime = currentTime;

    timeRemaining -= delta;
    return timeRemaining <= 0;
  }

  /**
   * Starts the timer for the current hall.
   */
  public void startTimer() {
    timerActive = true;
    timeRemaining = hallTimeLimits[currentHall];
    lastUpdateTime = System.currentTimeMillis();
  }

  /**
   * Pauses the timer.
   */
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

  /**
   * Resumes the timer.
   */
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
        timeRemaining = timeLimit;
      }
    }
  }

  /**
   * Gets the current time remaining in milliseconds.
   */
  public long getTimeRemaining() {
    return timeRemaining;
  }

  /**
   * Resets the timer state.
   */
  public void resetTimer() {
    timerActive = false;
    timeRemaining = hallTimeLimits[currentHall];
    lastUpdateTime = System.currentTimeMillis();
  }

  /**
   * Updates enchantment states, spawning new ones and removing expired ones.
   * Should be called each game update.
   */
  public void updateEnchantments() {
    // Remove expired enchantments
    enchantments.removeIf(Enchantment::hasExpired);

    // Check if it's time to spawn a new enchantment
    long currentTime = System.currentTimeMillis();
    long effectiveTime = currentTime - pauseDuration;
    long effectiveLastSpawnTime = lastEnchantmentSpawnTime - pauseDuration;

    if (effectiveTime - effectiveLastSpawnTime >= ENCHANTMENT_SPAWN_INTERVAL) {
      spawnRandomEnchantment();
      lastEnchantmentSpawnTime = currentTime;
    }
  }

  /**
   * Spawns a random enchantment at a random empty location.
   */
  private void spawnRandomEnchantment() {
    // Get a random empty position
    int[] position = findRandomEmptyPosition();
    if (position == null)
      return;

    // Choose a random enchantment type
    Enchantment.Type[] types = Enchantment.Type.values();
    Enchantment.Type randomType = types[new Random().nextInt(types.length)];

    // Create and add the enchantment
    enchantments.add(new Enchantment(randomType, position[0], position[1]));
  }

  /**
   * Gets the list of active enchantments.
   */
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
      if (x >= enchantment.getX() && x < enchantment.getX() + tileSize &&
          y >= enchantment.getY() && y < enchantment.getY() + tileSize) {
        enchantments.remove(enchantment);
        return enchantment;
      }
    }
    return null;
  }

  /**
   * Called when the game is unpaused.
   * Updates pause duration for all time-sensitive entities.
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
  }

  /**
   * Resets the enchantment spawn timer.
   * Called when transitioning between halls or starting the game.
   */
  public void resetEnchantmentSpawnTimer() {
    lastEnchantmentSpawnTime = System.currentTimeMillis();
    // Clear any existing enchantments
    enchantments.clear();
  }

  /**
   * Handles the collection of an enchantment.
   * Immediate effect enchantments are applied directly,
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
      // TODO: Implement extra life effect
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

  /**
   * Resets the enchantment inventory.
   * Called when resetting the game state.
   */
  public void resetEnchantmentInventory() {
    for (Enchantment.Type type : STORABLE_ENCHANTMENTS) {
      enchantmentInventory.put(type, 0);
    }
  }
}