package ui.tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import ui.ui_main.GamePanel;

/**
 * Manages tiles and the game's map, including loading tile images,
 * parsing map layouts, and rendering tiles on the screen.
 */
public class TileManager {

  /**
   * The main game panel instance, used for accessing game configuration and
   * rendering properties.
   */
  GamePanel gp;

  /**
   * Array of available tile types.
   */
  Tile[] tile;

  /**
   * 2D array representing the tile numbers in the map.
   */
  int mapTileNum[][];

  /**
   * Initializes the TileManager with the provided GamePanel.
   * 
   * @param gp The game panel instance that manages the game's primary
   *           configurations.
   */
  public TileManager(GamePanel gp) {
    this.gp = gp;

    // Initialize tile array and map structure
    tile = new Tile[10];
    mapTileNum = new int[gp.maxScreenCol][gp.maxScreenRow];

    getTileImage();
    loadMap("/maps/map01.txt");
  }

  /**
   * Loads the tile images and associates them with tile types.
   */
  public void getTileImage() {
    try {
      // Initialize floor tile
      tile[0] = new Tile();
      tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/floor_plain.png"));

      // Initialize wall tile
      tile[1] = new Tile();
      tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/Wall_front.png"));
    } catch (IOException e) {
      // Log and handle exceptions related to loading tile images
      e.printStackTrace();
    }
  }

  /**
   * Loads a map file and populates the `mapTileNum` array with tile information.
   * 
   * @param filePath Path to the map file to be loaded.
   */
  public void loadMap(String filePath) {
    try {
      // Open the map file for reading
      InputStream is = getClass().getResourceAsStream(filePath);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      int col = 0;
      int row = 0;

      // Read the map line by line
      while (col < gp.maxScreenCol && row < gp.maxScreenRow) {
        String line = br.readLine();

        while (col < gp.maxScreenCol) {
          String numbers[] = line.split(" ");

          // Parse the tile number and store it in the map array
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
      // Handle exceptions gracefully
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

    // Iterate through the map grid
    while (col < gp.maxScreenCol && row < gp.maxScreenRow) {

      // Get the tile number at the current position
      int tileNum = mapTileNum[col][row];

      // Draw the tile at the calculated position
      g2.drawImage(tile[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);
      col++;

      // Increment the x-coordinate for the next tile
      x += gp.tileSize;

      // Move to the next row when the current row is complete
      if (col == gp.maxScreenCol) {
        col = 0;
        x = 0;
        row++;
        y += gp.tileSize;
      }
    }
  }
}
