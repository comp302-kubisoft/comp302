package domain.model.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import ui.tile.TileManager;

public class Hero extends Entity {

  private BufferedImage image;
  private boolean spawnPositionSet = false;

  public Hero() {
    setDefaultValues();
    loadImage();
  }

  private void setDefaultValues() {
    speed = 4;
    direction = "down";
    x = 0;
    y = 0;
  }

  public void setSpawnPosition(TileManager tileManager, int tileSize) {
    if (spawnPositionSet) return;
    int[][] map = tileManager.mapTileNum;
    int totalCols = map.length;
    int totalRows = map[0].length;

    int topWall = totalRows, bottomWall = 0, leftWall = totalCols, rightWall = 0;

    for (int col = 0; col < totalCols; col++) {
      for (int row = 0; row < totalRows; row++) {
        if (map[col][row] == 1) {
          if (row < topWall) topWall = row;
          if (row > bottomWall) bottomWall = row;
          if (col < leftWall) leftWall = col;
          if (col > rightWall) rightWall = col;
        }
      }
    }

    int centerCol = (leftWall + rightWall) / 2;
    int centerRow = (topWall + bottomWall) / 2;
    while (tileManager.tile[map[centerCol][centerRow]].collision) {
      centerRow++;
    }

    x = centerCol * tileSize;
    y = centerRow * tileSize;
    spawnPositionSet = true;
  }

  public void moveIfPossible(int dx, int dy, TileManager tileManager, int tileSize) {
    int nextX = x + dx;
    int nextY = y + dy;

    if (!tileManager.checkTileCollision(nextX, nextY, tileSize, tileSize)) {
      x = nextX;
      y = nextY;
    }
  }

  private void loadImage() {
    try {
      image = ImageIO.read(getClass().getResourceAsStream("/hero/player.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public BufferedImage getImage() {
    return image;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getSpeed() {
    return speed;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }
}
