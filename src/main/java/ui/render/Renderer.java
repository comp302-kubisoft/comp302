package ui.render;

import domain.model.GameMode;
import domain.model.GameState;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import ui.menu.Menu;

public class Renderer {

    private GameState gameState;
    private int tileSize;
    private int screenWidth;
    private int screenHeight;
    private Menu menu;

    private final Color BACKGROUND_DARK = new Color(72, 44, 52);
    private final Color WOOD_DARK = new Color(87, 61, 38);
    private final Color WOOD_LIGHT = new Color(116, 82, 53);
    private final Color TEXT_COLOR = new Color(231, 231, 231);

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
        g2.setColor(BACKGROUND_DARK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        int margin = 40;
        int panelWidth = screenWidth - 2 * margin;
        int panelHeight = screenHeight - 2 * margin;
        
        GradientPaint woodGradient = new GradientPaint(
            margin, margin, WOOD_DARK,
            margin + panelWidth, margin + panelHeight, WOOD_LIGHT);
        g2.setPaint(woodGradient);
        g2.fillRect(margin, margin, panelWidth, panelHeight);
        
        g2.setColor(WOOD_DARK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(margin, margin, panelWidth, panelHeight);
        
        int plankHeight = 30;
        g2.setStroke(new BasicStroke(2));
        for (int y = margin + plankHeight; y < margin + panelHeight; y += plankHeight) {
            g2.drawLine(margin, y, margin + panelWidth, y);
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 40));
        String title = "HELP SCREEN";
        
        g2.setColor(BACKGROUND_DARK);
        g2.drawString(title, screenWidth/2 - 120 + 2, screenHeight/4 + 2);
        
        g2.setColor(TEXT_COLOR);
        g2.drawString(title, screenWidth/2 - 120, screenHeight/4);

        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        String[] helpText = {
            "CONTROLS",
            "",
            "WASD or Arrow Keys: Move character",
            "ESC: Return to menu",
            "",
            "OBJECTIVE",
            "",
            "Explore the dungeon and survive!"
        };

        int startY = screenHeight/2 - 100;
        int lineHeight = 35;
        
        for (String line : helpText) {
            int textWidth = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, screenWidth/2 - textWidth/2, startY);
            startY += lineHeight;
        }

        String hint = "Press ESC to return";
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        int hintY = screenHeight - 100;
        int textWidth = g2.getFontMetrics().stringWidth(hint);
        int boxWidth = textWidth + 40;
        int boxHeight = 30;
        int boxX = screenWidth/2 - boxWidth/2;
        int boxY = hintY - 20;
        
        g2.setColor(WOOD_DARK);
        g2.fillRect(boxX, boxY, boxWidth, boxHeight);
        g2.setColor(WOOD_LIGHT);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(boxX, boxY, boxWidth, boxHeight);
        
        g2.setColor(TEXT_COLOR);
        g2.drawString(hint, screenWidth/2 - textWidth/2, hintY);
    }
}