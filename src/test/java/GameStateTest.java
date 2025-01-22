import static org.junit.jupiter.api.Assertions.*;

import domain.model.GameState;
import domain.model.entity.Monster;
import domain.model.entity.Monster.Type;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameStateTest {

  private GameState gameState;

  @BeforeEach
  void setUp() {
    // Instantiate a GameState with tileSize, columns, rows
    gameState = new GameState(16, 28, 20);
  }

  @Test
  void testInitialRepOk() {
    // The newly constructed gameState should satisfy repOk
    assertTrue(gameState.repOk(), "Initial gameState should be OK according to repOk.");
  }

  @Test
  void testAddAndRemovePlacedObject() {
    // Add a placed object in the first hall
    gameState.setCurrentHall(0);
    int x = 32, y = 32; // pixel coordinates
    int gridX = x / 16, gridY = y / 16;
    gameState.addPlacedObject(0, x, y, gridX, gridY);

    // Ensure the object is there
    List<GameState.PlacedObject> hallObjects = gameState.getPlacedObjects();
    assertEquals(1, hallObjects.size());
    assertTrue(gameState.repOk());

    // Remove that object
    gameState.removePlacedObject(gridX, gridY);
    assertEquals(0, gameState.getPlacedObjects().size());
    assertTrue(gameState.repOk());
  }

  @Test
  void testMonsterAddition() {
    // Create a monster
    Monster monster = new Monster(Type.FIGHTER, 50, 50);
    boolean added = gameState.addMonster(monster);

    assertTrue(added, "Should successfully add monster to the list.");
    assertTrue(gameState.getMonsters().contains(monster));
    assertTrue(gameState.repOk());
  }

  @Test
  void testSetCurrentHallOutOfRangeFailsRepOk() {
    // If we push currentHall out of range, repOk should fail
    gameState.setCurrentHall(GameState.TOTAL_HALLS); // out of range
    assertFalse(gameState.repOk(), "repOk should fail if currentHall is out of range.");
  }
}
