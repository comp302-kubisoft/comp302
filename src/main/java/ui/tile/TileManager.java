package ui.tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import ui.ui_main.GamePanel;

/**
 * Manages tiles and the game's map, including loading tile images, parsing map layouts, and
 * rendering tiles on the screen.
 */
public class TileManager {

  /** The main game panel instance, used for accessing game configuration and rendering properties. */
  GamePanel gp;

  /** Array of available tile types. */
  public Tile[] tile;

  /** 2D array representing the tile numbers in the map. */
  public int mapTileNum[][];

  /**
   * Initializes the TileManager with the provided GamePanel.
   *
   * @param gp The game panel instance that manages the game's primary configurations.
   */
  public TileManager(GamePanel gp) {
    this.gp = gp;

    // Initialize tile array and map structure
    tile = new Tile[10];
    mapTileNum = new int[gp.maxScreenCol][gp.maxScreenRow];

    getTileImage();
    loadMap("/maps/map01.txt");
  }

  /** Loads the tile images and associates them with tile types. */
  public void getTileImage() {
    try {
      // Initialize floor tile
      tile[0] = new Tile();
      tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/floor_plain.png"));
      tile[0].collision = false; // FLOOR TILE IS PASSABLE

      // Initialize wall tile
      tile[1] = new Tile();
      tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/Wall_front.png"));
      tile[1].collision = true; // WALL TILE IS IMPASSABLE - COLLISION SET TO TRUE

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * CHECKS FOR COLLISIONS AT ALL FOUR CORNERS OF THE HERO'S BOUNDING BOX.
   *
   * @param x The x-coordinate of the hero's position in pixels.
   * @param y The y-coordinate of the hero's position in pixels.
   * @param width The width of the hero (in pixels).
   * @param height The height of the hero (in pixels).
   * @return True if any of the corners intersect a collidable tile, false otherwise.
   */
  public boolean checkTileCollision(int x, int y, int width, int height) {
    // CALCULATE TILE COORDINATES FOR THE FOUR CORNERS
    int leftCol = x / gp.tileSize;
    int rightCol = (x + width) / gp.tileSize;
    int topRow = y / gp.tileSize;
    int bottomRow = (y + height) / gp.tileSize;

    // CHECK IF ANY CORNER IS OUT OF BOUNDS
    if (leftCol < 0 || rightCol >= gp.maxScreenCol || topRow < 0 || bottomRow >= gp.maxScreenRow) {
      return true; // Treat out-of-bounds as collidable
    }

    // CHECK TILE COLLISIONS AT ALL FOUR CORNERS
    boolean topLeftCollision = tile[mapTileNum[leftCol][topRow]].collision;
    boolean topRightCollision = tile[mapTileNum[rightCol][topRow]].collision;
    boolean bottomLeftCollision = tile[mapTileNum[leftCol][bottomRow]].collision;
    boolean bottomRightCollision = tile[mapTileNum[rightCol][bottomRow]].collision;

    return topLeftCollision || topRightCollision || bottomLeftCollision || bottomRightCollision;
  }

  /**
   * Loads a map file and populates the `mapTileNum` array with tile information.
   *
   * @param filePath Path to the map file to be loaded.
   */
  public void loadMap(String filePath) {
    try {
      InputStream is = getClass().getResourceAsStream(filePath);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      int col = 0;
      int row = 0;

      while (col < gp.maxScreenCol && row < gp.maxScreenRow) {
        String line = br.readLine();

        while (col < gp.maxScreenCol) {
          String numbers[] = line.split(" ");
          int num = Integer.parseInt(numbers[col]);
          mapTileNum[col][row] = num;
          col++;
        }
        if (col == gp.maxScreenCol) {
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
   * Draws the map on the screen using the tiles and their corresponding images.
   *
   * @param g2 The Graphics2D object used for rendering.
   */
  public void draw(Graphics2D g2) {
    int col = 0;
    int row = 0;
    int x = 0;
    int y = 0;

    while (col < gp.maxScreenCol && row < gp.maxScreenRow) {
      int tileNum = mapTileNum[col][row];
      g2.drawImage(tile[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);

      col++;
      x += gp.tileSize;

      if (col == gp.maxScreenCol) {
        col = 0;
        x = 0;
        row++;
        y += gp.tileSize;
      }
    }
  }
}
