package ui.tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileManager {

  private int tileSize;
  private int maxScreenCol;
  private int maxScreenRow;
  public Tile[] tile;
  public int[][] mapTileNum;

  public TileManager(int tileSize, int maxScreenCol, int maxScreenRow) {
    this.tileSize = tileSize;
    this.maxScreenCol = maxScreenCol;
    this.maxScreenRow = maxScreenRow;
    tile = new Tile[10];
    mapTileNum = new int[maxScreenCol][maxScreenRow];
    getTileImage();
    loadMap("/maps/map01.txt");
  }

  public void getTileImage() {
    try {
      tile[0] = TileFactory.createTile("/tiles/floor_plain.png", false);
      tile[1] = TileFactory.createTile("/tiles/Wall_front.png", true);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

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
}
