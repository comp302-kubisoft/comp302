package ui.render;

import domain.model.GameMode;
import domain.model.GameState;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import ui.menu.Menu;
import ui.tile.BuildObjectManager;

public class Renderer {

    private GameState gameState;
    private int tileSize;
    private int screenWidth;
    private int screenHeight;
    private Menu menu;
    private BuildObjectManager buildObjectManager;

    private final Color BACKGROUND_DARK = new Color(72, 44, 52);
    private final Color WOOD_DARK = new Color(87, 61, 38);
    private final Color WOOD_LIGHT = new Color(116, 82, 53);
    private final Color TEXT_COLOR = new Color(231, 231, 231);

    private int selectedObjectIndex = -1; // -1 means no selection

    public Renderer(GameState gameState, int tileSize, int screenWidth, int screenHeight) {
        this.gameState = gameState;
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.buildObjectManager = new BuildObjectManager();
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public void render(Graphics2D g2, GameMode currentMode) {
    	int currentHallIndex = gameState.getCurrentHallIndex();
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
                // Draw placed objects first (behind the hero)
                for (GameState.PlacedObject obj : gameState.getObjectsForHall(currentHallIndex)) {
                    g2.drawImage(buildObjectManager.getImage(obj.type),
                            obj.x, obj.y, tileSize, tileSize, null);
                }
                // Draw hero on top
                g2.drawImage(gameState.getHero().getImage(),
                        gameState.getHero().getX(),
                        gameState.getHero().getY(),
                        tileSize,
                        tileSize,
                        null);
                break;
            case BUILD:
                drawBuildMode(g2);
                drawProgressButton(g2, screenWidth, screenHeight, currentHallIndex == 3); // Draw the button
                break;
        }
    }

    public void render(Graphics2D g2, GameMode currentMode, Menu menu) {
        this.menu = menu;
        render(g2, currentMode);
    }

    public void setSelectedObject(int index) {
        this.selectedObjectIndex = index;
    }

    public int getSelectedObjectIndex() {
        return selectedObjectIndex;
    }

    private void drawBuildMode(Graphics2D g2) {
    	int currentHallIndex = gameState.getCurrentHallIndex();
        // Draw the main game area
        gameState.getTileManager().draw(g2);

        // Draw placed objects
        for (GameState.PlacedObject obj : gameState.getObjectsForHall(currentHallIndex)) {
            g2.drawImage(buildObjectManager.getImage(obj.type),
                    obj.x, obj.y, tileSize, tileSize, null);
        }

        // Draw the build panel on the right
        int panelMargin = 10;
        int panelWidth = screenWidth / 5;
        int panelX = screenWidth - panelWidth - panelMargin;
        int panelHeight = screenHeight - 2 * panelMargin;
        int panelY = panelMargin;

        // Draw panel background with gradient
        GradientPaint woodGradient = new GradientPaint(
                panelX, panelY, WOOD_DARK,
                panelX + panelWidth, panelY + panelHeight, WOOD_LIGHT);
        g2.setPaint(woodGradient);
        g2.fillRect(panelX, panelY, panelWidth, panelHeight);

        // Draw panel border
        g2.setColor(WOOD_DARK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(panelX, panelY, panelWidth, panelHeight);

        // Draw title
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.setColor(TEXT_COLOR);
        String title = "Build Mode";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, panelX + (panelWidth - titleWidth) / 2, panelY + 30);

        // Draw object slots with smaller size
        int slotMargin = 10;
        int slotSize = (panelWidth - 2 * slotMargin) / 2; // Half the previous size
        int slotY = panelY + 50;
        int slotSpacing = slotSize + 15; // Reduced spacing

        for (int i = 0; i < buildObjectManager.getObjectCount(); i++) {
            int currentSlotY = slotY + i * slotSpacing;

            // Draw selection highlight if this object is selected
            if (i == selectedObjectIndex) {
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRect(panelX + slotMargin - 2, currentSlotY - 2, slotSize + 4, slotSize + 4);
            }

            // Draw slot background
            g2.setColor(WOOD_DARK);
            g2.fillRect(panelX + slotMargin, currentSlotY, slotSize, slotSize);

            // Draw object image
            g2.drawImage(buildObjectManager.getImage(i),
                    panelX + slotMargin, currentSlotY, slotSize, slotSize, null);
        }
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
        g2.drawString(title, screenWidth / 2 - 120 + 2, screenHeight / 4 + 2);

        g2.setColor(TEXT_COLOR);
        g2.drawString(title, screenWidth / 2 - 120, screenHeight / 4);

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

        int startY = screenHeight / 2 - 100;
        int lineHeight = 35;

        for (String line : helpText) {
            int textWidth = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, screenWidth / 2 - textWidth / 2, startY);
            startY += lineHeight;
        }

        String hint = "Press ESC to return";
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));

        int hintY = screenHeight - 100;
        int textWidth = g2.getFontMetrics().stringWidth(hint);
        int boxWidth = textWidth + 40;
        int boxHeight = 30;
        int boxX = screenWidth / 2 - boxWidth / 2;
        int boxY = hintY - 20;

        g2.setColor(WOOD_DARK);
        g2.fillRect(boxX, boxY, boxWidth, boxHeight);
        g2.setColor(WOOD_LIGHT);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(boxX, boxY, boxWidth, boxHeight);

        g2.setColor(TEXT_COLOR);
        g2.drawString(hint, screenWidth / 2 - textWidth / 2, hintY);
    }
    
    public void drawProgressButton(Graphics2D g2, int screenWidth, int screenHeight, boolean isLastHall) {
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonX = (screenWidth - buttonWidth) / 2;
        int buttonY = screenHeight - 100;

        g2.setColor(Color.BLUE);
        g2.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        g2.setColor(Color.WHITE);
        g2.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String label = isLastHall ? "Play Game" : "Build Next Hall";
        g2.drawString(label, buttonX + 40, buttonY + 30);
    }
}