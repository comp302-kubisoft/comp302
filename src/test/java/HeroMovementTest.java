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
        
        // Set hero's initial position
        hero.setPosition(0, 0);
        
        // Ensure the map is free of obstacles
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                tileManager.mapTileNum[i][j] = 0; // 0 = empty
            }
        }
    }

    @Test
    void testSuccessfulMovementRight() {
        hero.setPosition(0, 0); // Ensure hero starts at (0, 0)
        int initialX = hero.getX();
        int initialY = hero.getY();
    
        // Move right by one tile (48 pixels)
        hero.moveIfPossible(48, 0, tileManager, 48);
    
        // Verify position changed
        assertEquals(initialX + 48, hero.getX(), "Hero should move right by 48 pixels");
        assertEquals(initialY, hero.getY(), "Y position should remain unchanged");
    }

    @Test
    void testSuccessfulMovementUp() {
        int initialX = hero.getX();
        int initialY = hero.getY();

        // Move up by one tile (48 pixels)
        hero.moveIfPossible(0, -48, tileManager, 48);

        // Verify position changed
        assertEquals(initialX, hero.getX(), "X position should remain unchanged");
        assertEquals(initialY - 48, hero.getY(), "Hero should move up by 48 pixels");
    }

    @Test
    void testDirectionUpdate() {
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

        // Move down
        hero.moveIfPossible(0, 48, tileManager, 48);
        assertEquals("down", hero.getDirection(), "Direction should update to down when moving down");
    }
}