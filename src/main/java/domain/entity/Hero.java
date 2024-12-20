package domain.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import ui.ui_main.GamePanel;
import ui.ui_main.InputState;

/**
 * Represents the player's controllable character in the game. Extends the generic Entity class to
 * include specific attributes and behaviors for the hero.
 */
public class Hero extends Entity {

  /** Reference to the game's main panel for accessing configurations and properties. */
  GamePanel gp;

  /** Reference to InputState for handling user input. */
  InputState inputState;

  /** FLAG TO ENSURE SPAWN POSITION IS SET ONLY ONCE. */
  private boolean spawnPositionSet = false;

  /**
   * Initializes the hero entity with default values, images, and input handling.
   *
   * @param gp The game panel instance for accessing game configurations.
   * @param inputState The input state instance that tracks player input.
   */
  public Hero(GamePanel gp, InputState inputState) {
    this.gp = gp;
    this.inputState = inputState;
    setDefaultValues();
    getPlayerImage();
  }

  /** Sets the default values for the hero's speed and initial direction. */
  public void setDefaultValues() {
    speed = 4;           // Default movement speed
    direction = "down";  // Default direction the hero is facing

    // TEMPORARY INITIAL POSITION UNTIL SPAWN POSITION IS SET
    x = 0;
    y = 0;
  }

  /** DYNAMICALLY SETS THE HERO'S SPAWN POSITION TO THE CENTER OF THE PASSABLE AREA INSIDE WALLS. */
  private void setSpawnPosition() {
    int[][] map = gp.gameState.getTileManager().mapTileNum; // GET MAP DATA
    int totalCols = map.length;       // TOTAL COLUMNS IN MAP
    int totalRows = map[0].length;    // TOTAL ROWS IN MAP

    int topWall = totalRows, bottomWall = 0, leftWall = totalCols, rightWall = 0;

    // FIND WALL BOUNDARIES (WHERE 1'S EXIST IN THE MAP)
    for (int col = 0; col < totalCols; col++) {
      for (int row = 0; row < totalRows; row++) {
        if (map[col][row] == 1) { // WALL TILE DETECTED
          if (row < topWall) topWall = row;            // UPDATE TOP WALL
          if (row > bottomWall) bottomWall = row;      // UPDATE BOTTOM WALL
          if (col < leftWall) leftWall = col;          // UPDATE LEFT WALL
          if (col > rightWall) rightWall = col;        // UPDATE RIGHT WALL
        }
      }
    }

    // CALCULATE CENTER OF PASSABLE AREA
    int centerCol = (leftWall + rightWall) / 2;  // CENTER COLUMN BASED ON WALLS
    int centerRow = (topWall + bottomWall) / 2;  // CENTER ROW BASED ON WALLS

    // FIND NEAREST PASSABLE TILE (0) INSIDE THE ENCLOSURE
    while (gp.gameState.getTileManager().tile[map[centerCol][centerRow]].collision) {
      centerRow++; // MOVE DOWN UNTIL PASSABLE TILE IS FOUND
    }

    // SET HERO POSITION TO CENTER OF TILE
    x = centerCol * gp.tileSize;
    y = centerRow * gp.tileSize;

    spawnPositionSet = true; // MARK AS SET
  }

  /** Updates the hero's state based on InputState, including position, direction, and collision. */
  public void update() {
    // CHECK AND SET SPAWN POSITION ON FIRST UPDATE
    if (!spawnPositionSet) {
      setSpawnPosition();
    }

    int nextX = x; // Tentative x-coordinate
    int nextY = y; // Tentative y-coordinate

    // CALCULATE NEXT POSITION BASED ON INPUTSTATE
    if (inputState.upPressed) {
      direction = "up";
      nextY -= speed; // Tentative move up
    }
    if (inputState.downPressed) {
      direction = "down";
      nextY += speed; // Tentative move down
    }
    if (inputState.leftPressed) {
      direction = "left";
      nextX -= speed; // Tentative move left
    }
    if (inputState.rightPressed) {
      direction = "right";
      nextX += speed; // Tentative move right
    }

    // CHECK FOR COLLISION BEFORE UPDATING POSITION
    if (!gp.gameState.getTileManager().checkTileCollision(nextX, nextY, gp.tileSize, gp.tileSize)) {
        x = nextX;
        y = nextY;
    }

  }

  /**
   * Renders the hero on the screen at its current position.
   *
   * @param g2 The Graphics2D object used for rendering the hero.
   */
  public void draw(Graphics2D g2) {
    BufferedImage image = imag;
    g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
  }

  /** Loads the hero's image from the resources folder. */
  public void getPlayerImage() {
    try {
      imag = ImageIO.read(getClass().getResourceAsStream("/hero/player.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
