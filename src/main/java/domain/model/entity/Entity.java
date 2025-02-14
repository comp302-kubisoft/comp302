/**
 * Base class for all game entities. Provides common functionality for position, movement, and
 * direction handling. Serves as the parent class for more specific entity types like Hero.
 */
package domain.model.entity;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public abstract class Entity implements Serializable {

  private static final long serialVersionUID = 1L;

  /** X coordinate in pixels */
  protected int x;

  /** Y coordinate in pixels */
  protected int y;

  /** Movement speed in pixels per update */
  protected int speed;

  /** Entity's sprite image */
  public transient BufferedImage left1, left2, right1, right2, up1, up2, down1, down2;

  /** Current facing direction ("up", "down", "left", "right") */
  protected String direction;

  /** Health of the entity */
  protected int health;

  /**
   * Gets the entity's current X coordinate.
   *
   * @return X position in pixels
   */
  public int getX() {
    return x;
  }

  /**
   * Gets the entity's current Y coordinate.
   *
   * @return Y position in pixels
   */
  public int getY() {
    return y;
  }

  /**
   * Gets the entity's movement speed.
   *
   * @return Speed in pixels per update
   */
  public int getSpeed() {
    return speed;
  }

  /**
   * Gets the entity's current facing direction.
   *
   * @return Direction string ("up", "down", "left", "right")
   */
  public String getDirection() {
    return direction;
  }

  /**
   * Sets the entity's facing direction.
   *
   * @param dir New direction to face ("up", "down", "left", "right")
   */
  public void setDirection(String dir) {
    this.direction = dir;
  }

  /**
   * Sets the entity's position.
   *
   * @param x New X coordinate in pixels
   * @param y New Y coordinate in pixels
   */
  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Sets the entity's X coordinate.
   *
   * @param x New X coordinate in pixels
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Sets the entity's Y coordinate.
   *
   * @param y New Y coordinate in pixels
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Sets the entity's movement speed.
   *
   * @param speed New speed in pixels per update
   */
  public void setSpeed(int speed) {
    this.speed = speed;
  }

  /**
   * Gets the entity's health.
   *
   * @return Health of the entity
   */
  public int getHealth() {
    return health;
  }

  /**
   * Sets the entity's health.
   *
   * @param health New health of the entity
   */
  public void setHealth(int health) {
    this.health = health;
  }
}
