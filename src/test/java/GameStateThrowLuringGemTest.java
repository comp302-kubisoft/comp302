import static org.junit.jupiter.api.Assertions.*;

import domain.model.GameState;
import domain.model.entity.Enchantment;
import domain.model.entity.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameStateThrowLuringGemTest {

    private GameState gameState;
    private Hero hero;
    private static final int TILE_SIZE = 48;
    private static final int MAX_SCREEN_COL = 28;
    private static final int MAX_SCREEN_ROW = 20;

    @BeforeEach
    void setUp() {
        // Create a new GameState before each test
        gameState = new GameState(TILE_SIZE, MAX_SCREEN_COL, MAX_SCREEN_ROW);

        hero = gameState.getHero();

        hero.setPosition(5 * TILE_SIZE, 5 * TILE_SIZE);

        // Add a luring gem to inventory
        gameState.handleEnchantmentCollection(new Enchantment(Enchantment.Type.LURING_GEM, 10, 10));
    }

    @Test
    void testThrowLuringGemTypicalCase() {
        // Test throwing gem in a direction with space available
        int initialGemCount = gameState.getEnchantmentCount(Enchantment.Type.LURING_GEM);

        gameState.throwLuringGem("right");

        assertTrue(gameState.isLuringGemActive());
        assertEquals(initialGemCount - 1,
                gameState.getEnchantmentCount(Enchantment.Type.LURING_GEM));
        assertTrue(gameState.getGemX() * TILE_SIZE > gameState.getHero().getX()); // Gem should be to the right of hero
    }

    @Test
    void testThrowLuringGemEdgeCase() {
        // Test throwing gem at game boundary
        Hero hero = gameState.getHero();
        hero.setPosition(gameState.getGameAreaEnd() * TILE_SIZE - TILE_SIZE, hero.getY());

        gameState.throwLuringGem("right");

        // Gem should be clamped to game boundary
        assertTrue(gameState.isLuringGemActive());
        assertTrue(gameState.getGemX() * TILE_SIZE <= gameState.getGameAreaEnd() * TILE_SIZE);
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