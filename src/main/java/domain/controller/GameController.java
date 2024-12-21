package domain.controller;

import domain.model.GameMode;
import domain.model.GameState;
import domain.model.entity.Hero;
import ui.input.InputState;
import ui.main.GamePanel;
import ui.menu.Menu;

public class GameController {

    private final GameState gameState;
    private final InputState inputState;
    private final GamePanel gamePanel;
    private boolean spawnPositionSet = false;
    private Menu menu;

    private static final int MIN_EARTH = 6;
    private static final int MIN_AIR = 9;
    private static final int MIN_WATER = 13;
    private static final int MIN_FIRE = 17;

    private int earthCount = 0;
    private int airCount = 0;
    private int waterCount = 0;
    private int fireCount = 0;

    public GameController(GameState gameState, InputState inputState, GamePanel gamePanel) {
        this.gameState = gameState;
        this.inputState = inputState;
        this.gamePanel = gamePanel;
        this.menu = new Menu();
        gamePanel.getRenderer().setMenu(menu);
    }

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

    public void updateBuildMode() {
        if (inputState.escapePressed) {
            gamePanel.setMode(GameMode.MENU);
            inputState.reset();
            gamePanel.resetGameState();
            return;
        }

        // Fake example logic: If user presses up/down/left/right, it places an object
        // in a certain hall
        // (In reality, you'd have specific code or mouse interactions for each hall.)
        if (inputState.upPressed) {
            earthCount++; // Suppose "up" adds an object to Earth Hall
            inputState.upPressed = false;
        }
        if (inputState.downPressed) {
            airCount++; // "down" adds to Air Hall
            inputState.downPressed = false;
        }
        if (inputState.leftPressed) {
            waterCount++; // "left" adds to Water Hall
            inputState.leftPressed = false;
        }
        if (inputState.rightPressed) {
            fireCount++; // "right" adds to Fire Hall
            inputState.rightPressed = false;
        }

        // Suppose pressing ENTER tries to finalize Build Mode and start the game
        if (inputState.enterPressed) {
            // Check if all halls satisfy minimum object count
            if (earthCount >= MIN_EARTH &&
                    airCount >= MIN_AIR &&
                    waterCount >= MIN_WATER &&
                    fireCount >= MIN_FIRE) {
                // Pass to first playable hall
                gamePanel.setMode(GameMode.PLAY);
                inputState.reset();
            } else {
                // In a real UI, show a message "Not enough objects placed..."
                System.out.println("Please place more objects to meet min requirements!");
            }
        }
    }

    public void updatePlayMode() {
        Hero hero = gameState.getHero();

        if (!spawnPositionSet) {
            hero.setSpawnPosition(gameState.getTileManager(), gamePanel.getTileSize());
            spawnPositionSet = true;
        }

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

        if (inputState.escapePressed) {
            gamePanel.setMode(GameMode.MENU);
            inputState.reset();

            // Reset the game state so starting a new game is always fresh
            gamePanel.resetGameState();
            return;
        }

        hero.setDirection(direction);
        hero.moveIfPossible(dx, dy, gameState.getTileManager(), gamePanel.getTileSize());
    }

    public Menu getMenu() {
        return menu;
    }
}