package domain.entity;

import java.awt.image.BufferedImage;

/**
 * Represents a generic game entity with basic properties such as position, speed, visual
 * representation, and movement direction.
 */
public class Entity {

  /** The x-coordinate of the entity's position on the game grid. */
  public int x;

  /** The y-coordinate of the entity's position on the game grid. */
  public int y;

  /** The movement speed of the entity. */
  public int speed;

  /** The image associated with the entity, used for rendering. */
  public BufferedImage imag;

  /** The current movement direction of the entity (e.g., "up", "down", "left", "right"). */
  public String direction;
}
