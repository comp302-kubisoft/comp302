/**
 * Handles mouse input events for the game's build mode.
 * This class manages object selection from the build panel and object placement
 * in the game area through mouse interactions.
 */
package ui.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import ui.render.Renderer;
import domain.model.GameState;
import ui.main.GamePanel;
import domain.model.GameMode;

public class MouseHandler extends MouseAdapter {
    /** Reference to the game renderer for object selection */
    private final Renderer renderer;
    /** Reference to the game state for object placement */
    private final GameState gameState;
    /** Size of each tile in pixels */
    private final int tileSize;
    /** Total width of the game screen */
    private final int screenWidth;
    /** Width of the build panel */
    private final int panelWidth;
    /** Margin around the build panel */
    private final int panelMargin;
    /** Reference to the game panel */
    private final GamePanel gamePanel;

    /**
     * Creates a new MouseHandler with necessary references for build mode
     * interaction.
     * 
     * @param renderer    Reference to the game renderer
     * @param gameState   Reference to the game state
     * @param tileSize    Size of each tile in pixels
     * @param screenWidth Total width of the game screen
     * @param gamePanel   Reference to the game panel
     */
    public MouseHandler(Renderer renderer, GameState gameState, int tileSize, int screenWidth, GamePanel gamePanel) {
        this.renderer = renderer;
        this.gameState = gameState;
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.panelWidth = screenWidth / 5;
        this.panelMargin = 10;
        this.gamePanel = gamePanel;
    }

    /**
     * Handles mouse click events in both the build panel and game area.
     * Determines if the click was in the build panel (for object selection)
     * or in the game area (for object placement).
     * 
     * @param e The mouse click event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Check if click is on the cross button
        if (renderer.isWithinCrossButton(x, y)) {
            if (gamePanel.getMode() == GameMode.PLAY || gamePanel.getMode() == GameMode.BUILD) {
                gamePanel.setMode(GameMode.MENU);
                gamePanel.resetGameState();
                return;
            }
        }

        // Check if click is on the pause button in play mode
        if (gamePanel.getMode() == GameMode.PLAY && renderer.isWithinPauseButton(x, y)) {
            renderer.togglePause();
            return;
        }

        // Handle object checking in play mode
        if (gamePanel.getMode() == GameMode.PLAY && !renderer.isPaused()) {
            // Convert click coordinates to grid position
            int gridX = x / tileSize;
            int gridY = y / tileSize;

            // Check if there's an object at the clicked position
            if (gameState.isTileOccupied(gridX, gridY)) {
                // Check if hero is adjacent to the object
                if (gameState.isHeroAdjacent(gridX, gridY, tileSize)) {
                    boolean foundRune = gameState.checkForRune(gridX, gridY);
                    // Show victory screen only if we've found all runes
                    if (foundRune && gameState.getRunesFound() == GameState.TOTAL_HALLS) {
                        System.out.println("Congratulations! You have found all the mystical runes!");
                        gamePanel.setMode(GameMode.VICTORY);
                    }
                }
            }
            return;
        }

        // Handle build mode clicks
        if (gamePanel.getMode() == GameMode.BUILD) {
            // Check if click is in the build panel
            if (x >= screenWidth - panelWidth - panelMargin) {
                handlePanelClick(x, y);
            } else {
                // Check if click is in the game area
                handleGameAreaClick(x, y);
            }
        }
    }

    /**
     * Handles clicks in the build panel for object selection.
     * Determines which object slot was clicked and updates the selected object.
     * 
     * @param x The x coordinate of the click
     * @param y The y coordinate of the click
     */
    private void handlePanelClick(int x, int y) {
        int panelX = screenWidth - panelWidth - panelMargin;
        int slotMargin = 10;
        int slotSize = (panelWidth - 2 * slotMargin) / 2;
        int slotY = panelMargin + 50;
        int slotSpacing = slotSize + 15;

        // Check which slot was clicked
        if (x >= panelX + slotMargin && x <= panelX + slotMargin + slotSize) {
            for (int i = 0; i < 5; i++) {
                int currentSlotY = slotY + i * slotSpacing;
                if (y >= currentSlotY && y <= currentSlotY + slotSize) {
                    renderer.setSelectedObject(i);
                    return;
                }
            }
        }
    }

    /**
     * Handles clicks in the game area for object placement.
     * Places the selected object at the clicked tile if the position is valid.
     * 
     * @param x The x coordinate of the click
     * @param y The y coordinate of the click
     */
    private void handleGameAreaClick(int x, int y) {
        // Only handle clicks if an object is selected
        if (renderer.getSelectedObjectIndex() == -1)
            return;

        // Convert click coordinates to grid position
        int gridX = x / tileSize;
        int gridY = y / tileSize;

        // Check if click is within the game area and tile is not occupied
        if (gameState.isWithinGameArea(gridX, gridY) && !gameState.isTileOccupied(gridX, gridY)) {
            gameState.addPlacedObject(
                    renderer.getSelectedObjectIndex(),
                    gridX * tileSize,
                    gridY * tileSize,
                    gridX,
                    gridY);
        }
    }
}