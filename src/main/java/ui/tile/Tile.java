/**
 * Represents a single tile in the game world. Contains the tile's image and collision property.
 * Used by TileManager to build the game map.
 */
package ui.tile;

import java.awt.image.BufferedImage;

public class Tile {
  /** The tile's sprite image */
  public BufferedImage image;

  /** Whether entities can pass through this tile */
  public boolean collision = false;
}
