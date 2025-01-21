import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import domain.model.GameState;

public class GameStateCheckForRuneTest {

    private GameState gameState;
    private static final int TILE_SIZE = 48;
    private static final int MAX_SCREEN_COL = 28;
    private static final int MAX_SCREEN_ROW = 20;

    @BeforeEach
    void setUp() {
        gameState = new GameState(TILE_SIZE, MAX_SCREEN_COL, MAX_SCREEN_ROW);
        // Ensure we're in hall 0
        gameState.setCurrentHall(0);
    }

    @Test
    void testFindRuneInObject() {
        // Setup: Place object with rune
        gameState.addPlacedObject(0, 96, 96, 2, 2);
        GameState.PlacedObject obj = gameState.getPlacedObjects().get(0);
        obj.hasRune = true;

        // Record initial state
        int initialRunesFound = gameState.getRunesFound();

        // Execute: Check for rune
        boolean result = gameState.checkForRune(2, 2);

        // Verify
        assertTrue(result, "Should return true when rune is found");
        assertEquals(initialRunesFound + 1, gameState.getRunesFound(),
                "Should increment runesFound");
        assertEquals(3, gameState.getTileManager().mapTileNum[9][18],
                "Should change door tile to open (3)");
    }

    @Test
    void testCheckEmptyLocation() {
        // Execute: Check location with no object
        boolean result = gameState.checkForRune(5, 5);

        // Verify
        assertFalse(result, "Should return false for empty location");
        assertEquals(0, gameState.getRunesFound(),
                "Should not change runesFound");
        assertNotEquals(3, gameState.getTileManager().mapTileNum[9][18],
                "Should not change door tile");
    }

    @Test
    void testCheckObjectWithoutRune() {
        // Setup: Place object without rune
        gameState.addPlacedObject(0, 144, 144, 3, 3);

        // Record initial state
        int initialRunesFound = gameState.getRunesFound();
        int initialDoorTile = gameState.getTileManager().mapTileNum[9][18];

        // Execute: Check for rune
        boolean result = gameState.checkForRune(3, 3);

        // Verify
        assertFalse(result, "Should return false when object has no rune");
        assertEquals(initialRunesFound, gameState.getRunesFound(),
                "Should not change runesFound");
        assertEquals(initialDoorTile, gameState.getTileManager().mapTileNum[9][18],
                "Should not change door tile");
    }
}