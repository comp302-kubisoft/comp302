package ui.ui_main;

import domain.entity.Hero;
import ui.tile.TileManager;

/**
 * Represents the game state, managing entities like the hero and tiles.
 * Acts as the Model in the MVC architecture.
 */
public class GameState {

    private Hero hero;             // Player's hero entity
    private TileManager tileManager; // TileManager for map and tiles
    private InputState inputState;   // INPUTSTATE INSTANCE ADDED

    /**
     * Initializes the game state with a Hero and TileManager.
     *
     * @param gamePanel The game panel for configuration.
     * @param keyHandler The key handler for input management.
     */
    public GameState(GamePanel gamePanel, KeyHandler keyHandler) {
        this.inputState = keyHandler.inputState; // GET INPUTSTATE FROM KEYHANDLER
        this.hero = new Hero(gamePanel, inputState); // PASS INPUTSTATE TO HERO
        this.tileManager = new TileManager(gamePanel);
    }

    /**
     * Updates the game state.
     * This method will update all game entities.
     */
    public void update() {
        hero.update(); // UPDATE HERO USING INPUTSTATE
        // Other updates can go here, such as enemies, items, etc.
    }

    /**
     * Gets the hero entity.
     *
     * @return The Hero instance.
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * Gets the TileManager instance.
     *
     * @return The TileManager instance.
     */
    public TileManager getTileManager() {
        return tileManager;
    }
}
