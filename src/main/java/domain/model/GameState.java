/**
 * Represents the core game state and manages all game entities and objects.
 * This class serves as the central model maintaining the current state of the game,
 * including the hero, placed objects, and game area boundaries.
 */
package domain.model;

import domain.model.entity.Hero;
import ui.main.GamePanel;
import ui.tile.TileManager;
import java.util.ArrayList;
import java.util.List;

public class GameState {

  /** The player character entity */
  private Hero hero;
  /** Manages the game's tile-based map */
  private TileManager tileManager;
  
  
  /** List of all objects placed in the game world */
  ///private List<PlacedObject> placedObjects;
  
  private List<List<PlacedObject>> hallObjects; // One list per hall
  
  /** Starting coordinate of the game area (inclusive) */
  private static final int GAME_AREA_START = 2;
  /** Ending coordinate of the game area (inclusive) */
  private static final int GAME_AREA_END = 17;
  
  private int currentHallIndex = 0;

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
    //this.placedObjects = new ArrayList<>();
    this.hero = new Hero(this);
    for (int i = 0; i < 4; i++) { // Four halls: earth, air, water, fire
        hallObjects.add(new ArrayList<>()); // Initialize a list for each hall
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
  public boolean isTileOccupied(int gridX, int gridY, int currentHallIndex) {
    for (PlacedObject obj : hallObjects.get(currentHallIndex)) {
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
  public void addPlacedObject(int type, int x, int y, int gridX, int gridY, int currentHallIndex) {
	  if (isWithinGameArea(gridX, gridY) && !isTileOccupied(gridX, gridY, currentHallIndex)) {
	        PlacedObject newObject = new PlacedObject(type, x, y, gridX, gridY);
	        hallObjects.get(currentHallIndex).add(newObject); // Add to the correct hall
	  }
  }

  /**
   * Gets the list of all objects placed in the game world.
   * 
   * @return List of PlacedObject instances
   */
  /*
  public List<PlacedObject> getPlacedObjects() {
    return placedObjects;
  }
  */
  
  public List<PlacedObject> getObjectsForHall(int hallIndex) {
	    return hallObjects.get(hallIndex);
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
  
  public int getCurrentHallIndex() {
	    return currentHallIndex;
  }

  public void incrementHallIndex() {
	    currentHallIndex++;
  }
  
  public boolean handleProgressButtonClick(int x, int y, int screenWidth, int screenHeight, GamePanel gamePanel) {
	    int buttonWidth = 200;
	    int buttonHeight = 50;
	    int buttonX = (screenWidth - buttonWidth) / 2;
	    int buttonY = screenHeight - 100;

	    if (x >= buttonX && x <= buttonX + buttonWidth && y >= buttonY && y <= buttonY + buttonHeight) {
	        if (currentHallIndex == 3) {
	            gamePanel.setMode(GameMode.PLAY); // Start gameplay after the last hall
	        } else {
	            incrementHallIndex(); // Move to the next hall
	            System.out.println("Moved to hall " + currentHallIndex);
	        }
	        return true; // Button was clicked
	    }
	    return false; // Button was not clicked
	}
}
