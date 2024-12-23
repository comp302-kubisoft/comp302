package ui.render;

import domain.model.GameMode;
import domain.model.GameState;
import domain.model.entity.Monster;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import ui.menu.Menu;
import ui.tile.BuildObjectManager;
import ui.main.GamePanel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;

public class Renderer {

    private GameState gameState;
    private int tileSize;
    private int screenWidth;
    private int screenHeight;
    private Menu menu;
    private BuildObjectManager buildObjectManager;
    private boolean isPaused = false;
    private BufferedImage heartImage;
    private GamePanel gamePanel;

    private final Color BACKGROUND_DARK = new Color(72, 44, 52);
    private final Color WOOD_DARK = new Color(87, 61, 38);
    private final Color WOOD_LIGHT = new Color(116, 82, 53);
    private final Color TEXT_COLOR = new Color(231, 231, 231);

    private int selectedObjectIndex = -1; // -1 means no selection

    public Renderer(GameState gameState, int tileSize, int screenWidth, int screenHeight, GamePanel gamePanel) {
        this.gameState = gameState;
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.buildObjectManager = new BuildObjectManager();
        this.gamePanel = gamePanel;
        loadHeartImage();
    }

    private void loadHeartImage() {
        try {
            heartImage = ImageIO.read(getClass().getResourceAsStream("/ui/heart.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                // Draw main game area
                gameState.getTileManager().draw(g2);

                // Draw placed objects first (behind the hero and monsters)
                for (GameState.PlacedObject obj : gameState.getPlacedObjects()) {
                    g2.drawImage(buildObjectManager.getImage(obj.type),
                            obj.x, obj.y, tileSize, tileSize, null);
                }

                // Draw monsters
                for (Monster monster : gameState.getMonsters()) {
                    g2.drawImage(monster.getImage(),
                            monster.getX(),
                            monster.getY(),
                            tileSize,
                            tileSize,
                            null);
                }

                // Draw hero on top
                g2.drawImage(gameState.getHero().getImage(),
                        gameState.getHero().getX(),
                        gameState.getHero().getY(),
                        tileSize,
                        tileSize,
                        null);

                // Draw right panel for UI elements
                drawPlayModePanel(g2);

                // Draw pause overlay if game is paused
                if (isPaused) {
                    drawPauseOverlay(g2);
                }

                // Draw pause and cross buttons (always on top)
                drawPauseButton(g2);
                drawCrossButton(g2);
                break;
            case BUILD:
                drawBuildMode(g2);
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
        // Draw the main game area
        gameState.getTileManager().draw(g2);

        // Draw placed objects
        for (GameState.PlacedObject obj : gameState.getPlacedObjects()) {
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

        // Draw cross button on top of everything
        drawCrossButton(g2);
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

    private void drawCrossButton(Graphics2D g2) {
        int buttonSize = 30;
        int margin = 10;
        int x = screenWidth - buttonSize - margin;
        int y = margin;

        // Draw button background
        g2.setColor(WOOD_DARK);
        g2.fillRect(x, y, buttonSize, buttonSize);
        g2.setColor(WOOD_LIGHT);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, buttonSize, buttonSize);

        // Draw X
        g2.setColor(TEXT_COLOR);
        g2.setStroke(new BasicStroke(2));
        int padding = 8;
        g2.drawLine(x + padding, y + padding, x + buttonSize - padding, y + buttonSize - padding);
        g2.drawLine(x + buttonSize - padding, y + padding, x + padding, y + buttonSize - padding);
    }

    public boolean isWithinCrossButton(int mouseX, int mouseY) {
        int buttonSize = 30;
        int margin = 10;
        int x = screenWidth - buttonSize - margin;
        int y = margin;

        return mouseX >= x && mouseX <= x + buttonSize &&
                mouseY >= y && mouseY <= y + buttonSize;
    }

    private void drawPauseButton(Graphics2D g2) {
        int buttonSize = 30;
        int margin = 10;
        int x = screenWidth - 2 * buttonSize - 2 * margin; // Position to the left of cross button
        int y = margin;

        // Draw button background
        g2.setColor(WOOD_DARK);
        g2.fillRect(x, y, buttonSize, buttonSize);
        g2.setColor(WOOD_LIGHT);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, buttonSize, buttonSize);

        // Draw pause/play symbol
        g2.setColor(TEXT_COLOR);
        g2.setStroke(new BasicStroke(2));
        int padding = 8;
        if (!isPaused) {
            // Draw pause symbol (two vertical lines)
            int lineWidth = 4;
            g2.fillRect(x + padding, y + padding, lineWidth, buttonSize - 2 * padding);
            g2.fillRect(x + buttonSize - padding - lineWidth, y + padding, lineWidth, buttonSize - 2 * padding);
        } else {
            // Draw play symbol (triangle)
            int[] xPoints = { x + padding, x + buttonSize - padding, x + padding };
            int[] yPoints = { y + padding, y + buttonSize / 2, y + buttonSize - padding };
            g2.fillPolygon(xPoints, yPoints, 3);
        }
    }

    public boolean isWithinPauseButton(int mouseX, int mouseY) {
        int buttonSize = 30;
        int margin = 10;
        int x = screenWidth - 2 * buttonSize - 2 * margin;
        int y = margin;

        return mouseX >= x && mouseX <= x + buttonSize &&
                mouseY >= y && mouseY <= y + buttonSize;
    }

    public void togglePause() {
        isPaused = !isPaused;
        gamePanel.getGameController().handlePauseState(isPaused);
    }

    public boolean isPaused() {
        return isPaused;
    }

    private void drawPauseOverlay(Graphics2D g2) {
        // Store the original composite
        java.awt.Composite originalComposite = g2.getComposite();

        // Create a semi-transparent grey overlay
        Color overlayColor = new Color(128, 128, 128, 180); // Grey with alpha
        g2.setColor(overlayColor);

        // Use alpha composite for transparency
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));

        // Draw the overlay rectangle
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Draw "PAUSED" text
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Monospaced", Font.BOLD, 48));
        String pausedText = "PAUSED";
        int textWidth = g2.getFontMetrics().stringWidth(pausedText);
        int textHeight = g2.getFontMetrics().getHeight();
        g2.drawString(pausedText, screenWidth / 2 - textWidth / 2, screenHeight / 2 - textHeight / 2);

        // Restore the original composite
        g2.setComposite(originalComposite);
    }

    private void drawPlayModePanel(Graphics2D g2) {
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
        String title = "Status";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, panelX + (panelWidth - titleWidth) / 2, panelY + 30);

        // Draw hearts
        if (heartImage != null) {
            int heartSize = 30;
            int heartY = panelY + 50;
            int heartSpacing = 5;
            int totalHeartsWidth = (heartSize * gameState.getHero().getMaxHealth()) +
                    (heartSpacing * (gameState.getHero().getMaxHealth() - 1));
            int heartsStartX = panelX + (panelWidth - totalHeartsWidth) / 2;

            // Draw all heart containers (grayed out)
            for (int i = 0; i < gameState.getHero().getMaxHealth(); i++) {
                int heartX = heartsStartX + (heartSize + heartSpacing) * i;
                // Draw grayed out heart
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2.drawImage(heartImage, heartX, heartY, heartSize, heartSize, null);
            }

            // Draw filled hearts for current health
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            for (int i = 0; i < gameState.getHero().getHealth(); i++) {
                int heartX = heartsStartX + (heartSize + heartSpacing) * i;
                g2.drawImage(heartImage, heartX, heartY, heartSize, heartSize, null);
            }
        }
    }
}