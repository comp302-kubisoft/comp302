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
import ui.sound.SoundManager;

public class GameController implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Reference to the current game state */
  private GameState gameState;

  /** Handles input state tracking (transient because we reinit after load) */
  private transient InputState inputState;

  /** Reference to the main game panel (transient because we reinit after load) */
  private transient GamePanel gamePanel;

  /** Manages menu state and interactions */
  private transient Menu menu;

  /** Tracks if hero spawn position has been set */
  private boolean spawnPositionSet = false;

  /** Time of last monster spawn */
  private long lastMonsterSpawnTime;

  /** Time when the game was last paused */
  private long pauseStartTime;

  /** Accumulated pause duration */
  private long pauseDuration;

  /** Interval between monster spawns in milliseconds */
  private static final long MONSTER_SPAWN_INTERVAL = 8000; // 8 seconds

  public GameController(GameState gameState, InputState inputState, GamePanel gamePanel) {
    this.gameState = gameState;
    this.inputState = inputState;
    this.gamePanel = gamePanel;
    this.menu = new Menu(SoundManager.getInstance());
    this.spawnPositionSet = false;
    if (gamePanel != null && gamePanel.getRenderer() != null) {
      gamePanel.getRenderer().setMenu(menu);
    }
  }

  /**
   * Reinitializes any transient fields after deserialization. Call this right
   * after loading
   * the GameController from file, supplying the new GamePanel and InputState
   * references.
   */
  public void reinitialize(GamePanel gamePanel, InputState inputState) {
    if (gamePanel == null || inputState == null) {
        throw new IllegalArgumentException("GamePanel and InputState cannot be null");
    }
    
    this.gamePanel = gamePanel;
    this.inputState = inputState;
    
    // Reset input state to clear any lingering inputs
    this.inputState.reset();
    
    if (this.menu == null) {
        this.menu = new Menu(SoundManager.getInstance());
    }
    if (gamePanel != null && gamePanel.getRenderer() != null) {
        gamePanel.getRenderer().setMenu(menu);
    }

    // Reinitialize transient fields in the GameState
    if (this.gameState != null) {
        this.gameState.reinitialize(
            gamePanel.tileSize,
            gamePanel.maxScreenCol,
            gamePanel.maxScreenRow);
    }

    // Don't trigger monster spawn on load
    this.lastMonsterSpawnTime = System.currentTimeMillis();
  }

  public GameState getGameState() {
    return this.gameState;
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
          // Call the GamePanel method to load the controller
          gamePanel.loadGameController("saves/" + selectedSave);
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
      } else if (inputState.rightPressed || inputState.leftPressed) {
        gamePanel.getRenderer().updateHelpPage(inputState.rightPressed);
        inputState.reset();
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

    // When enter is pressed, check minimum objects requirement for ALL halls
    if (inputState.enterPressed) {
      // Check all halls for minimum requirements
      for (int hall = 0; hall < GameState.TOTAL_HALLS; hall++) {
        int objectCount = gameState.getPlacedObjectsInHall(hall).size();
        int requiredObjects = getRequiredObjects(hall);

        if (objectCount < requiredObjects) {
          // Show warning message through renderer
          gamePanel.getRenderer().showWarningMessage(
              "Minimum " + requiredObjects + " objects required in " + getHallName(hall)
          );
          inputState.reset();
          return;
        }
      }

      // If we get here, all halls meet requirements
      // Assign random runes to all halls
      for (int hall = 0; hall < GameState.TOTAL_HALLS; hall++) {
        gameState.setCurrentHall(hall);
        gameState.assignRandomRune();
      }

      // Start play mode with the first hall
      gameState.setCurrentHall(0);
      startPlayMode();
    }
  }

  private int getRequiredObjects(int hall) {
    switch (hall) {
      case 0: return 6;  // Earth
      case 1: return 9;  // Air
      case 2: return 13; // Water
      case 3: return 17; // Fire
      default: return 0;
    }
  }

  private String getHallName(int hall) {
    switch (hall) {
      case 0: return "Hall of Earth";
      case 1: return "Hall of Air";
      case 2: return "Hall of Water";
      case 3: return "Hall of Fire";
      default: return "Unknown Hall";
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

    // Update monster spawning with timing check
    long currentTime = System.currentTimeMillis();
    long effectiveTime = currentTime - pauseDuration;
    if (effectiveTime - lastMonsterSpawnTime >= MONSTER_SPAWN_INTERVAL) {
        spawnMonster();
        lastMonsterSpawnTime = currentTime;
    }

    // Update monster states and interactions
    gameState.updateMonsters();

    // Update enchantments
    gameState.updateEnchantments();

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

  private void spawnMonster() {
    Hero hero = gameState.getHero();
    int heroX = hero.getX();
    int heroY = hero.getY();
    
    // Reduced safe radius to 2 tiles for more balanced spawning
    int safeRadius = 2;  // Changed from 3 to 2
    
    // Try to find a valid spawn position with maximum attempts
    int maxAttempts = 20;
    for (int attempt = 0; attempt < maxAttempts; attempt++) {
        // Get random position
        int[] position = gameState.findRandomEmptyPosition();
        if (position == null) return;  // No empty positions available
        
        // Check if position is far enough from hero
        int distanceX = Math.abs(position[0] - heroX);
        int distanceY = Math.abs(position[1] - heroY);
        
        // Only spawn if the monster is outside the safe radius
        if (distanceX > safeRadius || distanceY > safeRadius) {
            // Randomly select any monster type
            Monster.Type monsterType = Monster.Type.values()[new Random().nextInt(Monster.Type.values().length)];
            
            // Create and add the monster
            Monster monster = new Monster(monsterType, position[0], position[1]);
            gameState.addMonster(monster);
            return;  // Successfully spawned monster
        }
    }
    // If we get here, couldn't find a valid spawn position after max attempts
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