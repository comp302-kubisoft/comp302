package ui.render;

import domain.model.GameMode;
import domain.model.GameState;
import java.awt.Graphics2D;
import ui.menu.Menu;

public class Renderer {

    private GameState gameState;
    private int tileSize;
    private int screenWidth;
    private int screenHeight;
    private Menu menu;

    public Renderer(GameState gameState, int tileSize, int screenWidth, int screenHeight) {
        this.gameState = gameState;
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setMenu(Menu menu) { 
        this.menu = menu;
    }

    public void render(Graphics2D g2, GameMode currentMode) {
        switch (currentMode) {
            case MENU:
                if (menu != null) {
                    menu.draw(g2, screenWidth, screenHeight);
                }
                break;
            case HELP:
                drawHelpScreen(g2);
                break;
            case PLAY:
                gameState.getTileManager().draw(g2);
                g2.drawImage(gameState.getHero().getImage(), 
                             gameState.getHero().getX(), 
                             gameState.getHero().getY(), 
                             tileSize, 
                             tileSize, 
                             null);
                break;
        }
    }

    public void render(Graphics2D g2, GameMode currentMode, Menu menu) {
        this.menu = menu;
        render(g2, currentMode);
    }

    private void drawHelpScreen(Graphics2D g2) {
        g2.setColor(java.awt.Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(java.awt.Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        g2.drawString("HELP SCREEN", screenWidth / 2 - 50, screenHeight / 2 - 100);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
        g2.drawString("Use arrow keys to move.", screenWidth / 2 - 100, screenHeight / 2 - 50);
        g2.drawString("Press ESC to return to the menu.", screenWidth / 2 - 150, screenHeight / 2);
    }
}