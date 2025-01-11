
import domain.model.GameState;
import domain.model.entity.Monster;
import domain.model.entity.Monster.Type;
import domain.model.GameState.PlacedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MonsterTest {

    private GameState gameState;
    private Monster wizardMonster;

    @BeforeEach
    void setUp() {
        gameState = new GameState(16, 28, 20);
        wizardMonster = new Monster(Type.WIZARD, 0, 0);
        wizardMonster.setGameState(gameState);
    }
    
    @Test
    void testTeleportRuneWithTwoObjects() {
        // Add two objects
        gameState.setCurrentHall(0);
        gameState.addPlacedObject(0, 32, 32, 2, 2); // Object 1
        gameState.addPlacedObject(0, 64, 64, 4, 4); // Object 2

        // Assign rune to the first object
        List<PlacedObject> objects = gameState.getPlacedObjects();
        objects.get(0).hasRune = true;

        // Call teleportRune
        wizardMonster.teleportRune();

        // Ensure the rune has moved to the second object
        assertFalse(objects.get(0).hasRune, "Rune should no longer be on the first object.");
        assertTrue(objects.get(1).hasRune, "Rune should have moved to the second object.");
    }
    
    @Test
    void testTeleportRuneWithSingleObject() {
        // Add one object
        gameState.setCurrentHall(0);
        gameState.addPlacedObject(0, 32, 32, 2, 2); // Only Object

        // Assign rune to this object
        List<PlacedObject> objects = gameState.getPlacedObjects();
        objects.get(0).hasRune = true;

        // Call teleportRune
        wizardMonster.teleportRune();

        // Ensure the rune stays with the same object
        assertTrue(objects.get(0).hasRune, "Rune should remain on the only available object.");
    }
    
    @Test
    void testTeleportRuneWithNoObjects() {
        // Ensure no objects are in the current hall
        assertTrue(gameState.getPlacedObjects().isEmpty(), "Current hall should have no objects.");

        // Call teleportRune
        wizardMonster.teleportRune();

        // No exceptions or changes should occur
        assertTrue(gameState.getPlacedObjects().isEmpty(), "There should still be no objects.");
    }
}
