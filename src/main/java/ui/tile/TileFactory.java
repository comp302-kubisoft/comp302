package ui.tile;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class TileFactory {

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
