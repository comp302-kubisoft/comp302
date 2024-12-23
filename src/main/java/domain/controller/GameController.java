/**
 * Controls the game logic and handles updates for different game modes.
 * Acts as the main controller coordinating between user input, game state, and rendering.
 */
package domain.controller;

import domain.model.GameMode;
import domain.model.GameState;
import domain.model.entity.Hero;
import domain.model.entity.Monster;
import ui.input.InputState;
import ui.main.GamePanel;
import ui.menu.Menu;
import java.util.Random;

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
    /** Time of last monster spawn */
    private long lastMonsterSpawnTime;
    /** Time when the game was last paused */
    private long pauseStartTime;
    /** Accumulated pause duration */
    private long pauseDuration;
    /** Interval between monster spawns in milliseconds */
    private static final long MONSTER_SPAWN_INTERVAL = 8000; // 8 seconds

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
            // Assign a random rune before transitioning
            gameState.assignRandomRune();
            // Initialize monster spawn timer when entering play mode
            lastMonsterSpawnTime = System.currentTimeMillis();
            pauseDuration = 0;
            gamePanel.setMode(GameMode.PLAY);
            inputState.reset();
        }
    }

    /**
     * Called when the game is paused or unpaused.
     * 
     * @param isPaused true if game is being paused, false if being unpaused
     */
    public void handlePauseState(boolean isPaused) {
        if (isPaused) {
            // Record when the pause started
            pauseStartTime = System.currentTimeMillis();
        } else {
            // Add the pause duration to total pause time
            pauseDuration += System.currentTimeMillis() - pauseStartTime;
        }
    }

    /**
     * Updates the play mode state.
     * Handles hero movement, spawn position, monster spawning, and game
     * interactions during gameplay.
     */
    public void updatePlayMode() {
        Hero hero = gameState.getHero();

        // Set initial spawn position if not set
        if (!spawnPositionSet) {
            hero.setSpawnPosition(gameState.getTileManager(), gamePanel.getTileSize());
            spawnPositionSet = true;
        }

        // Handle monster spawning and updates only if not paused
        if (!gamePanel.getRenderer().isPaused()) {
            long currentTime = System.currentTimeMillis();
            // Adjust the comparison time by subtracting pause duration
            long adjustedTime = currentTime - pauseDuration;
            if (adjustedTime - lastMonsterSpawnTime >= MONSTER_SPAWN_INTERVAL) {
                spawnRandomMonster();
                lastMonsterSpawnTime = adjustedTime;
            }

            // Update monster states and interactions
            gameState.updateMonsters();
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
     * Spawns a random monster at a random empty location.
     * Only spawns if we haven't reached the maximum monster limit.
     */
    private void spawnRandomMonster() {
        // Get a random empty position
        int[] position = gameState.findRandomEmptyPosition();
        if (position == null)
            return; // No empty positions available

        // Choose a random monster type
        Monster.Type[] monsterTypes = Monster.Type.values();
        Monster.Type randomType = monsterTypes[new Random().nextInt(monsterTypes.length)];

        // Create and add the monster
        Monster monster = new Monster(randomType, position[0], position[1]);
        if (!gameState.addMonster(monster)) {
            // Monster wasn't added because we're at max capacity
            return;
        }
    }

    /**
     * Gets the current menu instance.
     * 
     * @return The Menu object managing menu state
     */
    public Menu getMenu() {
        return menu;
    }
}