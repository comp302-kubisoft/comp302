package domain.controller;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Manages saving/loading of the entire game via serialization of the GameController. */
public class SaveLoadManager {

  private static final String SAVE_DIRECTORY = "saves";

  static {
    // Ensure the saves directory exists
    try {
      Files.createDirectories(Paths.get(SAVE_DIRECTORY));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Saves the given GameController (which contains the GameState) to disk.
   *
   * @param gameController The current game controller to save
   * @param saveName A friendly name for the save file (no extension)
   */
  public static void saveGame(GameController gameController, String saveName) {
    if (gameController == null || saveName == null || saveName.isEmpty()) {
      throw new IllegalArgumentException("Invalid save parameters");
    }

    String filePath = SAVE_DIRECTORY + File.separator + saveName + ".ser";
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(gameController);
      System.out.println("Game saved successfully to " + filePath);
      gameController.getGameState().setSaveMessage("Game Saved Successfully!");
    } catch (IOException e) {
      System.err.println("Failed to save game: " + e.getMessage());
      gameController.getGameState().setSaveMessage("Failed to Save Game!");
      throw new RuntimeException("Failed to save game", e);
    }
  }

  /**
   * Loads a GameController from the specified file path.
   *
   * @param filePath The path to the .ser file
   * @return The deserialized GameController, or null if loading fails
   */
  public static GameController loadGame(String filePath) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      Object obj = ois.readObject();
      if (obj instanceof GameController) {
        System.out.println("Game loaded successfully from " + filePath);
        return (GameController) obj;
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns a list of available save files from the saves directory.
   *
   * @return A list of file names (without path) found in the saves/ directory
   */
  public static List<String> getAvailableSaves() {
    List<String> saves = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream =
        Files.newDirectoryStream(Paths.get(SAVE_DIRECTORY), "*.ser")) {
      for (Path path : directoryStream) {
        saves.add(path.getFileName().toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return saves;
  }

  public static boolean isValidSaveFile(String filePath) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      Object obj = ois.readObject();
      return obj instanceof GameController;
    } catch (Exception e) {
      return false;
    }
  }

  public static class SaveMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String saveName;
    public final long timestamp;
    public final int currentHall;
    public final int heroHealth;

    public SaveMetadata(GameController controller) {
      this.saveName = "Save_" + System.currentTimeMillis();
      this.timestamp = System.currentTimeMillis();
      this.currentHall = controller.getGameState().getCurrentHall();
      this.heroHealth = controller.getGameState().getHero().getHealth();
    }
  }
}
