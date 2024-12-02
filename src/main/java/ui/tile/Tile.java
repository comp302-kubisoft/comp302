package ui.tile;

import java.awt.image.BufferedImage;

/**
 * Represents a single tile in the game, which may include visual and collision
 * properties.
 */
public class Tile {

  /**
   * The image associated with this tile.
   */
  public BufferedImage image;

  /**
   * Indicates whether this tile has collision properties.
   * If true, the tile is impassable for game entities.
   */
  public boolean collision = false;
}
