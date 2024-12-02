package domain.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import ui.ui_main.GamePanel;
import ui.ui_main.KeyHandler;

/**
 * Represents the player's controllable character in the game.
 * Extends the generic Entity class to include specific attributes and behaviors
 * for the hero.
 */
public class Hero extends Entity {

  /**
   * Reference to the game's main panel for accessing configurations and
   * properties.
   */
  GamePanel gp;

  /**
   * Handles input from the user to control the hero's movement.
   */
  KeyHandler keyH;

  /**
   * Initializes the hero entity with default values, images, and input handling.
   * 
   * @param gp   The game panel instance for accessing game configurations.
   * @param keyH The key handler instance for managing player input.
   */
  public Hero(GamePanel gp, KeyHandler keyH) {
    this.gp = gp;
    this.keyH = keyH;
    setDefaultValues();
    getPlayerImage();
  }

  /**
   * Sets the default values for the hero's position, speed, and initial
   * direction.
   */
  public void setDefaultValues() {
    x = 100; // Default x-coordinate for the hero's starting position
    y = 100; // Default y-coordinate for the hero's starting position
    speed = 4; // Default movement speed
    direction = "down"; // Default direction the hero is facing
  }

  /**
   * Loads the hero's image from the resources folder.
   */
  public void getPlayerImage() {
    try {
      imag = ImageIO.read(getClass().getResourceAsStream("/hero/player.png"));
    } catch (IOException e) {
      // Log and handle any exceptions during image loading
      e.printStackTrace();
    }
  }

  /**
   * Updates the hero's state based on user input, including position and
   * direction.
   */
  public void update() {
    if (keyH.upPressed) {
      direction = "up";
      y -= speed; // Move up by reducing the y-coordinate
    } else if (keyH.downPressed) {
      direction = "down";
      y += speed; // Move down by increasing the y-coordinate
    } else if (keyH.leftPressed) {
      direction = "left";
      x -= speed; // Move left by reducing the x-coordinate
    } else if (keyH.rightPressed) {
      direction = "right";
      x += speed; // Move right by increasing the x-coordinate
    }
  }

  /**
   * Renders the hero on the screen at its current position.
   * 
   * @param g2 The Graphics2D object used for rendering the hero.
   */
  public void draw(Graphics2D g2) {
    BufferedImage image = imag; // Use the hero's image for rendering
    g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null); // Draw the hero on the screen
  }
}
