/**
 * Represents the core game state and manages all game entities and objects.
 * This class serves as the central model maintaining the current state of the game,
 * including the hero, placed objects, and game area boundaries.
 */
package domain.model;

import domain.model.entity.Hero;
import ui.tile.TileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import domain.model.entity.Monster;

public class GameState {

  /** The player character entity */
  private Hero hero;
  /** Manages the game's tile-based map */
  private TileManager tileManager;
  /** List of all objects placed in the game world */
  private List<PlacedObject> placedObjects;
  /** List of all monsters in the game */
  private List<Monster> monsters;

  /** Starting coordinate of the game area (inclusive) */
  private static final int GAME_AREA_START = 2;
  /** Ending coordinate of the game area (inclusive) */
  private static final int GAME_AREA_END = 17;
  /** Maximum number of monsters allowed in play mode */
  private static final int MAX_MONSTERS = 5;

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
   * Initializes a new game state with specified dimensions.
   */
  public GameState(int tileSize, int maxScreenCol, int maxScreenRow) {
    this.tileManager = new TileManager(tileSize, maxScreenCol, maxScreenRow);
    this.placedObjects = new ArrayList<>();
    this.monsters = new ArrayList<>();
    this.hero = new Hero(this);
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
   * Checks if a tile at the given grid position is occupied by any placed object.
   * 
   * @param gridX X coordinate to check
   * @param gridY Y coordinate to check
   * @return true if the tile is occupied, false if it's empty
   */
  public boolean isTileOccupied(int gridX, int gridY) {
    for (PlacedObject obj : placedObjects) {
      if (obj.gridX == gridX && obj.gridY == gridY) {
        return true;
      }
    }
    return false;
  }

  /**
   * Attempts to add a new object to the game world.
   * The object will only be placed if the position is within the game area
   * and the target tile is not already occupied.
   * 
   * @param type  Type of object to place
   * @param x     Pixel X coordinate
   * @param y     Pixel Y coordinate
   * @param gridX Grid X coordinate
   * @param gridY Grid Y coordinate
   */
  public void addPlacedObject(int type, int x, int y, int gridX, int gridY) {
    if (isWithinGameArea(gridX, gridY) && !isTileOccupied(gridX, gridY)) {
      placedObjects.add(new PlacedObject(type, x, y, gridX, gridY));
    }
  }

  /**
   * Gets the list of all objects placed in the game world.
   * 
   * @return List of PlacedObject instances
   */
  public List<PlacedObject> getPlacedObjects() {
    return placedObjects;
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
   * Assigns a rune to a random placed object.
   * Should be called when transitioning from build to play mode.
   */
  public void assignRandomRune() {
    if (placedObjects.isEmpty()) {
      return;
    }

    // Clear any existing runes
    for (PlacedObject obj : placedObjects) {
      obj.hasRune = false;
    }

    // Select a random object to have the rune
    int randomIndex = new java.util.Random().nextInt(placedObjects.size());
    PlacedObject selectedObject = placedObjects.get(randomIndex);
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
    // Get hero's grid position
    int heroGridX = hero.getX() / tileSize;
    int heroGridY = hero.getY() / tileManager.getTileSize();

    // Check if hero is in any adjacent tile
    return (Math.abs(heroGridX - gridX) == 1 && heroGridY == gridY) || // Left or right
        (Math.abs(heroGridY - gridY) == 1 && heroGridX == gridX); // Up or down
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
    for (PlacedObject obj : placedObjects) {
      if (obj.gridX == gridX && obj.gridY == gridY) {
        if (obj.hasRune) {
          System.out.println("You found a mystical rune!");
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
}