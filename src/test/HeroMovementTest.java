import static org.junit.jupiter.api.Assertions.*;
import domain.model.GameState;
import domain.model.entity.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.tile.TileManager;

public class HeroMovementTest {
    private GameState gameState;
    private Hero hero;
    private TileManager tileManager;

    @BeforeEach
    void setUp() {
        gameState = new GameState(48, 20, 20);
        hero = gameState.getHero();
        tileManager = gameState.getTileManager();
    }

    @Test
    void testMoveDirectionUpdate() {
        // Initial direction
        hero.setDirection("down");
        
        // Move right
        hero.moveIfPossible(48, 0, tileManager, 48);
        assertEquals("right", hero.getDirection(), "Direction should update to right when moving right");
        
        // Move left
        hero.moveIfPossible(-48, 0, tileManager, 48);
        assertEquals("left", hero.getDirection(), "Direction should update to left when moving left");
        
        // Move up
        hero.moveIfPossible(0, -48, tileManager, 48);
        assertEquals("up", hero.getDirection(), "Direction should update to up when moving up");
    }

    @Test
    void testSuccessfulMovement() {
        int initialX = hero.getX();
        int initialY = hero.getY();
        
        // Move right by one tile (48 pixels)
        hero.moveIfPossible(48, 0, tileManager, 48);
        
        // Verify position changed
        assertEquals(initialX + 48, hero.getX(), "Hero should move right by 48 pixels");
        assertEquals(initialY, hero.getY(), "Y position should remain unchanged");
    }

    @Test
    void testDiagonalMovementPrevention() {
        int initialX = hero.getX();
        int initialY = hero.getY();
        
        // Try to move diagonally (both dx and dy non-zero)
        hero.moveIfPossible(48, 48, tileManager, 48);
        
        // Verify only one direction changed
        assertTrue(
            (hero.getX() == initialX && hero.getY() == initialY + 48) ||
            (hero.getX() == initialX + 48 && hero.getY() == initialY),
            "Hero should not move diagonally"
        );
    }
}
