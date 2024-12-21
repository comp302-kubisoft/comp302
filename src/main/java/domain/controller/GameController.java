/**
 * Controls the game logic and handles updates for different game modes.
 * Acts as the main controller coordinating between user input, game state, and rendering.
 */
package domain.controller;

import domain.model.GameMode;
import domain.model.GameState;
import domain.model.entity.Hero;
import ui.input.InputState;
import ui.main.GamePanel;
import ui.menu.Menu;

public class GameController {

    /** Reference to the current game state */
    private final GameState gameState;
    /** Handles input state tracking */
    private final InputState inputState;
    /** Reference to the main game panel */
    private final GamePanel gamePanel;
    /** Tracks if hero spawn position has been set */
    private boolean spawnPositionSet = false;
    /** Manages menu state and interactions */
    private Menu menu;

    /** Minimum required objects for Earth hall */
    private static final int MIN_EARTH = 6;
    /** Minimum required objects for Air hall */
    private static final int MIN_AIR = 9;
    /** Minimum required objects for Water hall */
    private static final int MIN_WATER = 13;
    /** Minimum required objects for Fire hall */
    private static final int MIN_FIRE = 17;

    /** Counter for Earth hall objects */
    private int earthCount = 0;
    /** Counter for Air hall objects */
    private int airCount = 0;
    /** Counter for Water hall objects */
    private int waterCount = 0;
    /** Counter for Fire hall objects */
    private int fireCount = 0;
    
    private static final int TOTAL_HALLS = 4;

    
    /**
     * Initializes the game controller with necessary references and initial state.
     * 
     * @param gameState  The game state to control
     * @param inputState The input state to monitor
     * @param gamePanel  The main game panel reference
     */
    public GameController(GameState gameState, InputState inputState, GamePanel gamePanel) {
        this.gameState = gameState;
        this.inputState = inputState;
        this.gamePanel = gamePanel;
        this.menu = new Menu();
        this.spawnPositionSet = false;
        gamePanel.getRenderer().setMenu(menu);
    }

    /**
     * Updates the menu and help modes based on user input.
     * Handles mode transitions and input processing for menu navigation.
     */
    public void updateMenuOrHelpMode() {
        if (gamePanel.getMode() == GameMode.MENU) {
            boolean up = inputState.upPressed;
            boolean down = inputState.downPressed;
            boolean enter = inputState.enterPressed;

            GameMode newMode = menu.handleInput(up, down, enter);

            if (newMode != GameMode.MENU) {
                gamePanel.setMode(newMode);
                inputState.reset();
            }
        } else if (gamePanel.getMode() == GameMode.HELP) {
            if (inputState.escapePressed) {
                gamePanel.setMode(GameMode.MENU);
                inputState.reset();
            }
        }
    }

    /**
     * Updates the build mode state.
     * Handles object placement, mode transitions, and build mode specific logic.
     */
    public void updateBuildMode() {
        if (inputState.escapePressed) {
            gamePanel.setMode(GameMode.MENU);
            inputState.reset();
            gamePanel.resetGameState();
            return;
        }

        // Handle object placement with keyboard (legacy/placeholder logic)
        if (inputState.upPressed) {
            earthCount++;
            inputState.upPressed = false;
        }
        if (inputState.downPressed) {
            airCount++;
            inputState.downPressed = false;
        }
        if (inputState.leftPressed) {
            waterCount++;
            inputState.leftPressed = false;
        }
        if (inputState.rightPressed) {
            fireCount++;
            inputState.rightPressed = false;
        }

        // Transition to play mode when ready
        if (inputState.enterPressed) {
            gamePanel.setMode(GameMode.PLAY);
            inputState.reset();
        }
    }

    /**
     * Updates the play mode state.
     * Handles hero movement, spawn position, and game interactions during gameplay.
     */
    public void updatePlayMode() {
        Hero hero = gameState.getHero();

        // Set initial spawn position if not set
        if (!spawnPositionSet) {
            hero.setSpawnPosition(gameState.getTileManager(), gamePanel.getTileSize());
            spawnPositionSet = true;
        }

        // Handle hero movement
        int dx = 0, dy = 0;
        String direction = hero.getDirection();

        if (inputState.upPressed) {
            direction = "up";
            dy = -hero.getSpeed();
        } else if (inputState.downPressed) {
            direction = "down";
            dy = hero.getSpeed();
        } else if (inputState.leftPressed) {
            direction = "left";
            dx = -hero.getSpeed();
        } else if (inputState.rightPressed) {
            direction = "right";
            dx = hero.getSpeed();
        }

        // Handle return to menu
        if (inputState.escapePressed) {
            gamePanel.setMode(GameMode.MENU);
            inputState.reset();
            gamePanel.resetGameState();
            return;
        }

        // Update hero state
        hero.setDirection(direction);
        hero.moveIfPossible(dx, dy, gameState.getTileManager(), gamePanel.getTileSize());
    }

    /**
     * Gets the current menu instance.
     * 
     * @return The Menu object managing menu state
     */
    public Menu getMenu() {
        return menu;
    }
    
    public void moveToNextHall() {
        saveCurrentHallState(); // Save the current hall's state
        gameState.incrementHallIndex();
        if (gameState.getCurrentHallIndex() >= TOTAL_HALLS) {
            gamePanel.setMode(GameMode.PLAY); // Start gameplay after the last hall
        } else {
            loadCurrentHallMap(); // Load the map for the next hall
        }
    }
    private void loadCurrentHallMap() {
        String mapPath = "/maps/map0" + (gameState.getCurrentHallIndex() + 1) + ".txt"; // picking the map file
        gameState.getTileManager().loadMap(mapPath);
    }

    public void saveCurrentHallState() {
        System.out.println("Saved state for hall " + (gameState.getCurrentHallIndex() + 1));
        // GameState already tracks placed objects per hall
    }
}