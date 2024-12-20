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
            return;
        }

        hero.setDirection(direction);
        hero.moveIfPossible(dx, dy, gameState.getTileManager(), gamePanel.getTileSize());
    }

    public Menu getMenu() {
        return menu;
    }
}