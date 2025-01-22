package domain.model.entity;

import domain.model.GameState;
import java.io.Serializable;

public class GoodSituationWizardStrategy implements WizardStrategy, Serializable {
  private static final long serialVersionUID = 1L;
  private static final long TELEPORT_INTERVAL = 3000; // 3 seconds
  private long lastTeleportTime;

  @Override
  public void init(Monster monster, GameState gameState) {
    // Initialize timing at the moment we adopt this strategy.
    lastTeleportTime = monster.getAdjustedTime();
  }

  @Override
  public void update(Monster monster, GameState gameState) {
    long currentTime = monster.getAdjustedTime();

    // Check if it's time to teleport the rune again
    if (currentTime - lastTeleportTime >= TELEPORT_INTERVAL) {
      // Reuse or adapt the existing function that teleports the rune
      monster.teleportRune();
      lastTeleportTime = currentTime;
    }
  }
}
