/**
 * Factory class for creating tile objects.
 * Provides a centralized way to create tiles with their images and collision properties.
 * Uses the factory pattern to encapsulate tile creation logic.
 */
package ui.tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TileFactory {

    /**
     * Creates a new tile with specified image and collision property.
     * Loads the tile's image from resources and sets its collision state.
     * 
     * @param imagePath Path to the tile's image resource
     * @param collision Whether the tile should have collision
     * @return A new Tile instance with the specified properties
     */
    public static Tile createTile(String imagePath, boolean collision) {
        Tile tile = new Tile();
        try {
            BufferedImage img = ImageIO.read(TileFactory.class.getResourceAsStream(imagePath));
            tile.image = img;
            tile.collision = collision;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tile;
    }

}
