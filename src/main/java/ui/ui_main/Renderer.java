package ui.ui_main;

import java.awt.Graphics2D;

/**
 * Handles rendering of the game state, including tiles, hero, and other visual elements.
 * Acts as the View in the MVC architecture.
 */
public class Renderer {

    private GameState gameState; // Reference to the game state

    /**
     * Initializes the Renderer with a reference to the GameState.
     *
     * @param gameState The game state to render.
     */
    public Renderer(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Renders the entire game state.
     *
     * @param g2 The Graphics2D object used for rendering.
     */
    public void render(Graphics2D g2) {
        // Render tiles
        gameState.getTileManager().draw(g2);

        // Render the hero
        gameState.getHero().draw(g2);

        // Additional rendering can go here (e.g., UI, enemies, etc.)
    }
}
