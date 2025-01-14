/**
 * Controls the game logic and handles updates for different game modes. Acts as the main controller
 * coordinating between user input, game state, and rendering.
 */
package domain.controller;

import domain.model.GameMode;
import domain.model.GameState;
import domain.model.entity.Hero;
import domain.model.entity.Monster;

import java.io.Serializable;
import java.util.Random;
import ui.input.InputState;
import ui.main.GamePanel;
import ui.menu.Menu;

public class GameController implements Serializable {
  private static final long serialVersionUID = 1L;

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
   * Updates the menu, help, and game over modes based on user input. Handles mode
   * transitions and
   * input processing for menu navigation.
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
    } else if (gamePanel.getMode() == GameMode.LOAD) {
      if (inputState.escapePressed) {
        gamePanel.setMode(GameMode.MENU);
        inputState.reset();
        return;
      }

      if (inputState.upPressed || inputState.downPressed) {
        gamePanel.getRenderer().updateLoadScreenSelection(inputState.upPressed);
        inputState.reset();
      }

      if (inputState.enterPressed) {
        String selectedSave = gamePanel.getRenderer().getSelectedSaveFile();
        if (selectedSave != null) {
          GameState loadedState = SaveLoadManager.loadGame("saves/" + selectedSave);
          if (loadedState != null) {
            // Reinitialize transient fields before setting the game state
            loadedState.reinitialize(gamePanel.tileSize, gamePanel.maxScreenCol, gamePanel.maxScreenRow);

            gamePanel.gameState = loadedState;
            gamePanel.getRenderer().setGameState(loadedState);
            gamePanel.setMode(GameMode.PLAY);
          }
        }
        inputState.reset();
      }
    } else if (gamePanel.getMode() == GameMode.HELP
        || gamePanel.getMode() == GameMode.GAME_OVER
        || gamePanel.getMode() == GameMode.VICTORY) {
      if (inputState.escapePressed) {
        // Store the current mode before changing it
        GameMode previousMode = gamePanel.getMode();
        gamePanel.setMode(GameMode.MENU);
        inputState.reset();
        gamePanel.resetGameState();
        // Restart music if returning from game over or victory screen
        if (previousMode == GameMode.GAME_OVER || previousMode == GameMode.VICTORY) {
          gamePanel.playMusic(0);
        }
      }
    }
  }

  /**
   * Updates the build mode state. Handles object placement, mode transitions, and
   * build mode
   * specific logic.
   */
  public void updateBuildMode() {
    if (inputState.escapePressed) {
      gamePanel.setMode(GameMode.MENU);
      inputState.reset();
      gamePanel.resetGameState();
      return;
    }

    // Handle hall navigation with left/right arrow keys
    if (inputState.leftPressed) {
      if (gameState.getCurrentHall() > 0) {
        gameState.setCurrentHall(gameState.getCurrentHall() - 1);
        inputState.reset();
      }
    } else if (inputState.rightPressed) {
      if (gameState.getCurrentHall() < GameState.TOTAL_HALLS - 1) {
        gameState.setCurrentHall(gameState.getCurrentHall() + 1);
        inputState.reset();
      }
    }

    // When enter is pressed, check minimum objects requirement and handle hall
    // transition
    if (inputState.enterPressed) {
      int currentHall = gameState.getCurrentHall();
      int objectCount = gameState.getPlacedObjects().size();
      int requiredObjects;

      // Define minimum object requirements for each hall
      switch (currentHall) {
        case 0:
          requiredObjects = 6;
          break;
        case 1:
          requiredObjects = 9;
          break;
        case 2:
          requiredObjects = 13;
          break;
        case 3:
          requiredObjects = 17;
          break;
        default:
          requiredObjects = 0;
      }

      // Check if the current hall meets the minimum object requirement
      if (objectCount < requiredObjects) {

        inputState.reset();
        return;
      }

      // Assign a random rune to the current hall
      gameState.assignRandomRune();

      // Move to next hall or start play mode
      if (gameState.getCurrentHall() < GameState.TOTAL_HALLS - 1) {
        // Move to next hall
        gameState.setCurrentHall(gameState.getCurrentHall() + 1);
        inputState.reset();
      } else {
        // All halls are complete, start play mode with the first hall
        gameState.setCurrentHall(0);
        startPlayMode();
      }
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
      gameState.pauseTimer();
    } else {
      // Calculate the duration of this pause
      long thisPauseDuration = System.currentTimeMillis() - pauseStartTime;
      // Update pause duration for all time-sensitive entities
      gameState.updatePauseDuration(thisPauseDuration);
      // Adjust monster spawn timer
      lastMonsterSpawnTime += thisPauseDuration;
      gameState.resumeTimer();
    }
  }

  /** Resets the monster spawn timer. Called when transitioning between halls. */
  public void resetMonsterSpawnTimer() {
    lastMonsterSpawnTime = System.currentTimeMillis() - pauseDuration;
  }

  /**
   * Updates the play mode state. Handles hero movement, spawn position, monster
   * spawning, and game
   * interactions during gameplay.
   */
  public void updatePlayMode() {
    Hero hero = gameState.getHero();

    // Check for game over condition
    if (hero.getHealth() <= 0 || (!gamePanel.getRenderer().isPaused() && gameState.updateTimer())) {
      gamePanel.setMode(GameMode.GAME_OVER);
      gamePanel.stopMusic(); // Stop the background music
      gamePanel.playSFX(3); // Play game over sound
      inputState.reset();
      return;
    }

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

      // Update enchantments
      gameState.updateEnchantments();
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

    // Check if hero is on transition tile after movement
    if (gameState.isHeroOnTransitionTile()) {
      boolean isVictory = gameState.handleHallTransition();
      if (isVictory) {
        gamePanel.setMode(GameMode.VICTORY);
        inputState.reset();
      } else {
        // Reset monster spawn timer when transitioning to a new hall
        resetMonsterSpawnTimer();
        // Reset enchantment spawn timer for the new hall
        gameState.resetEnchantmentSpawnTimer();
        // Set and start timer for new hall
        gameState.setHallTimeLimit(gameState.getCurrentHall());
        gameState.startTimer();
      }
    }

    // Handle reveal enchantment usage
    if (inputState.revealPressed) {
      gameState.useRevealEnchantment();
      inputState.revealPressed = false;
    }

    // Update reveal effect
    gameState.updateRevealEffect();

    // Handle cloak enchantment usage
    if (inputState.cloakPressed) {
      gameState.useCloakEnchantment();
      inputState.cloakPressed = false;
    }

    // Update cloak effect
    gameState.updateCloakEffect();

    // Handle luring gem usage
    if (inputState.luringGemPressed) {
      inputState.throwGemActive = true;
      inputState.luringGemPressed = false;
    }

    if (inputState.throwGemActive && inputState.throwDirection != null) {
      gameState.throwLuringGem(inputState.throwDirection);
      inputState.throwGemActive = false;
      inputState.throwDirection = null;
    }

    gameState.updateLuringGemEffect();
  }

  /**
   * Spawns a random monster at a random empty location. Only spawns if we haven't
   * reached the
   * maximum monster limit.
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

  /**
   * Main update method that handles all game state updates based on the current
   * mode.
   *
   * @param currentMode The current game mode
   * @param isPaused    Whether the game is currently paused
   */
  public void update(GameMode currentMode, boolean isPaused) {
    switch (currentMode) {
      case MENU:
      case HELP:
      case LOAD:
        updateMenuOrHelpMode();
        break;
      case GAME_OVER:
      case VICTORY:
        updateMenuOrHelpMode();
        break;
      case PLAY:
        if (!isPaused) {
          updatePlayMode();
        }
        break;
      case BUILD:
        updateBuildMode();
        break;
    }
  }

  private void startPlayMode() {
    // Initialize monster spawn timer when entering play mode
    lastMonsterSpawnTime = System.currentTimeMillis();
    pauseDuration = 0;
    // Reset enchantment spawn timer
    gameState.resetEnchantmentSpawnTimer();
    // Set and start timer for first hall
    gameState.setHallTimeLimit(0);
    gameState.startTimer();
    gamePanel.setMode(GameMode.PLAY);
    inputState.reset();
  }
}