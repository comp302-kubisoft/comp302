/**
 * Manages the loading and access of object images used in build mode. This class is responsible for
 * loading and storing all placeable object sprites, providing a centralized way to access object
 * images by their index.
 */
package ui.tile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BuildObjectManager {
  /** Array storing all loaded object images */
  private BufferedImage[] objectImages;

  /** Paths to all object image resources */
  private static final String[] IMAGE_PATHS = {
    "/objects/chest.png",
    "/objects/barrel.png",
    "/objects/torch.png",
    "/objects/skull.png",
    "/objects/vase.png"
  };

  private static final String[] OBJECT_NAMES = {"Chest", "Barrel", "Torch", "Skull", "Vase"};

  /**
   * Initializes the BuildObjectManager and loads all object images. Images are loaded from the
   * resources specified in IMAGE_PATHS.
   */
  public BuildObjectManager() {
    loadImages();
  }

  /**
   * Loads all object images from their resource paths. Called during initialization to populate the
   * objectImages array.
   */
  private void loadImages() {
    objectImages = new BufferedImage[IMAGE_PATHS.length];
    try {
      for (int i = 0; i < IMAGE_PATHS.length; i++) {
        objectImages[i] = ImageIO.read(getClass().getResourceAsStream(IMAGE_PATHS[i]));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Retrieves an object's image by its index.
   *
   * @param index The index of the object image to retrieve
   * @return The BufferedImage for the specified object, or null if index is invalid
   */
  public BufferedImage getImage(int index) {
    if (index >= 0 && index < objectImages.length) {
      return objectImages[index];
    }
    return null;
  }

  /**
   * Gets the total number of available object types.
   *
   * @return The number of different objects that can be placed
   */
  public int getObjectCount() {
    return objectImages.length;
  }

  public String getObjectName(int index) {
    if (index >= 0 && index < OBJECT_NAMES.length) {
      return OBJECT_NAMES[index];
    }
    return "";
  }
}
