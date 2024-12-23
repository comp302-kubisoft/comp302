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

public class GameState {

  /** The player character entity */
  private Hero hero;
  /** Manages the game's tile-based map */
  private TileManager tileManager;
  /** List of all objects placed in the game world */
  private List<PlacedObject> placedObjects;

  /** Starting coordinate of the game area (inclusive) */
  private static final int GAME_AREA_START = 2;
  /** Ending coordinate of the game area (inclusive) */
  private static final int GAME_AREA_END = 17;

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
   * 
   * @param tileSize     Size of each tile in pixels
   * @param maxScreenCol Number of columns in the game world
   * @param maxScreenRow Number of rows in the game world
   */
  public GameState(int tileSize, int maxScreenCol, int maxScreenRow) {
    this.tileManager = new TileManager(tileSize, maxScreenCol, maxScreenRow);
    this.placedObjects = new ArrayList<>();
    this.hero = new Hero(this);
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
    int heroGridY = hero.getY() / tileSize;

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
}