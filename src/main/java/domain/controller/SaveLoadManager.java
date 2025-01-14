package domain.controller;

import domain.model.GameState;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

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
     * Saves the given GameState to disk with the specified saveName.
     *
     * @param gameState The current game state to save
     * @param saveName  A friendly name for the save file (no extension)
     */
    public static void saveGame(GameState gameState, String saveName) {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            // Write the entire GameState object graph (including all Serializable classes).
            oos.writeObject(gameState);
            System.out.println("Game saved successfully to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a GameState from the specified file path.
     *
     * @param filePath The path to the .ser file
     * @return The deserialized GameState, or null if loading fails
     */
    public static GameState loadGame(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object obj = ois.readObject();
            if (obj instanceof GameState) {
                System.out.println("Game loaded successfully from " + filePath);
                return (GameState) obj;
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
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(SAVE_DIRECTORY), "*.ser")) {
            for (Path path : directoryStream) {
                saves.add(path.getFileName().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saves;
    }
}