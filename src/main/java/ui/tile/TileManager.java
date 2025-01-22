/**
 * Manages the game's tile-based map system. Handles loading, storing, and rendering of the game
 * world's tiles. Provides collision detection functionality for game entities.
 */
package ui.tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

public class TileManager implements Serializable {

  private static final long serialVersionUID = 1L;

  // Door position constants
  private static final int DOOR_X = 9;  // Door X position in grid
  private static final int DOOR_Y = 16; // Door Y position in grid
  
  // Door tile indices
  private static final int CLOSED_DOOR_TILE = 2;  // Index for closed door tile
  private static final int OPEN_DOOR_TILE = 3;    // Index for open door tile

  /** Size of each tile in pixels */
  private int tileSize;

  /** Number of columns in the map */
  private int maxScreenCol;

  /** Number of rows in the map */
  private int maxScreenRow;

  /** Array of available tile types */
  public Tile[] tile;

  /** 2D array representing the map layout */
  public int[][] mapTileNum;

  private boolean doorOpen = false;

  /**
   * Creates a new TileManager with specified dimensions. Initializes tiles and loads the default
   * map.
   *
   * @param tileSize Size of each tile in pixels
   * @param maxScreenCol Number of columns in the map
   * @param maxScreenRow Number of rows in the map
   */
  public TileManager(int tileSize, int maxScreenCol, int maxScreenRow) {
    this.tileSize = tileSize;
    this.maxScreenCol = maxScreenCol;
    this.maxScreenRow = maxScreenRow;
    tile = new Tile[10];
    mapTileNum = new int[maxScreenCol][maxScreenRow];
    getTileImage();
    loadMap("/maps/map01.txt");
  }

  /**
   * Loads tile images and sets their collision properties. Initializes different types of tiles
   * used in the game.
   */
  public void getTileImage() {
    try {
      tile[0] = TileFactory.createTile("/tiles/floor_plain.png", false);
      tile[1] = TileFactory.createTile("/tiles/Wall_front.png", true);
      tile[2] = TileFactory.createTile("/tiles/door_closed.png", true);
      tile[3] = TileFactory.createTile("/tiles/door_open.png", true);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks if a given position would result in a collision with solid tiles. Used for entity
   * movement validation.
   *
   * @param x X coordinate to check
   * @param y Y coordinate to check
   * @param width Width of the entity
   * @param height Height of the entity
   * @return true if collision would occur, false otherwise
   */
  public boolean checkTileCollision(int x, int y, int width, int height) {
    int leftCol = x / tileSize;
    int rightCol = (x + width) / tileSize;
    int topRow = y / tileSize;
    int bottomRow = (y + height) / tileSize;

    if (leftCol < 0 || rightCol >= maxScreenCol || topRow < 0 || bottomRow >= maxScreenRow) {
      return true;
    }

    boolean topLeftCollision = tile[mapTileNum[leftCol][topRow]].collision;
    boolean topRightCollision = tile[mapTileNum[rightCol][topRow]].collision;
    boolean bottomLeftCollision = tile[mapTileNum[leftCol][bottomRow]].collision;
    boolean bottomRightCollision = tile[mapTileNum[rightCol][bottomRow]].collision;

    return topLeftCollision || topRightCollision || bottomLeftCollision || bottomRightCollision;
  }

  /**
   * Loads a map from a text file. Map file should contain space-separated tile indices.
   *
   * @param filePath Path to the map file in resources
   */
  public void loadMap(String filePath) {
    try {
      InputStream is = getClass().getResourceAsStream(filePath);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      int col = 0;
      int row = 0;

      while (col < maxScreenCol && row < maxScreenRow) {
        String line = br.readLine();

        while (col < maxScreenCol) {
          String[] numbers = line.split(" ");
          int num = Integer.parseInt(numbers[col]);
          mapTileNum[col][row] = num;
          col++;
        }
        if (col == maxScreenCol) {
          col = 0;
          row++;
        }
      }
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Renders the tile map to the screen. Draws each tile based on the map layout.
   *
   * @param g2 Graphics context to draw with
   */
  public void draw(Graphics2D g2) {
    int col = 0;
    int row = 0;
    int x = 0;
    int y = 0;

    while (col < maxScreenCol && row < maxScreenRow) {
      int tileNum = mapTileNum[col][row];
      g2.drawImage(tile[tileNum].image, x, y, tileSize, tileSize, null);
      col++;
      x += tileSize;

      if (col == maxScreenCol) {
        col = 0;
        x = 0;
        row++;
        y += tileSize;
      }
    }
  }

  /**
   * Gets the size of each tile in pixels.
   *
   * @return The tile size
   */
  public int getTileSize() {
    return tileSize;
  }

  public boolean isDoorOpen() {
    return doorOpen;
  }

  public void setDoorState(boolean isOpen) {
    this.doorOpen = isOpen;
    // Update the tile map when door state changes
    if (isOpen) {
      mapTileNum[DOOR_X][DOOR_Y] = OPEN_DOOR_TILE;
    } else {
      mapTileNum[DOOR_X][DOOR_Y] = CLOSED_DOOR_TILE;
    }
  }
}
