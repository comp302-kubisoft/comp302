import static org.junit.jupiter.api.Assertions.*;

import domain.model.GameState;
import domain.model.entity.Enchantment;
import domain.model.entity.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class GameStateThrowLuringGemTest {

    private GameState gameState;
    private static final int TILE_SIZE = 16;
    private static final int MAX_SCREEN_COL = 28;
    private static final int MAX_SCREEN_ROW = 20;

    @BeforeEach
    void setUp() {
        // Create a new GameState before each test
        gameState = new GameState(TILE_SIZE, MAX_SCREEN_COL, MAX_SCREEN_ROW);
        
        // Add a luring gem to inventory
        gameState.handleEnchantmentCollection(new Enchantment(Enchantment.Type.LURING_GEM, 0, 0));
    }

    @Test
    void testThrowLuringGemTypicalCase() {
        // Test throwing gem in a direction with space available
        int initialGemCount = gameState.getEnchantmentCount(Enchantment.Type.LURING_GEM);
        
        gameState.throwLuringGem("right");

        assertTrue(gameState.isLuringGemActive());
        assertEquals(initialGemCount - 1, 
            gameState.getEnchantmentCount(Enchantment.Type.LURING_GEM));
        assertTrue(gameState.getGemX() > gameState.getHero().getX()); // Gem should be to the right of hero
    }

    @Test
    void testThrowLuringGemEdgeCase() {
        // Test throwing gem at game boundary
        Hero hero = gameState.getHero();
        hero.setPosition(gameState.getGameAreaEnd() * TILE_SIZE - TILE_SIZE, hero.getY());
        
        gameState.throwLuringGem("right");

        // Gem should be clamped to game boundary
        assertTrue(gameState.isLuringGemActive());
        assertTrue(gameState.getGemX() <= gameState.getGameAreaEnd() * TILE_SIZE);
    }

    @Test
    void testThrowLuringGemErrorCase() {
        // Test throwing gem with empty inventory
        gameState.resetEnchantmentInventory(); // Reset inventory to empty
        
        // Try to throw a gem when inventory is empty
        gameState.throwLuringGem("right");

        // Nothing should change
        assertFalse(gameState.isLuringGemActive());
        assertEquals(0, gameState.getEnchantmentCount(Enchantment.Type.LURING_GEM));
    }
} 