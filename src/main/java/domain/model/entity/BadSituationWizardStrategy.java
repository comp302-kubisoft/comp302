package domain.model.entity;

import domain.model.GameState;

public class BadSituationWizardStrategy implements WizardStrategy {

    private boolean hasMovedHero = false;
    private static final long EXISTENCE_DURATION = 2000; // 2 seconds after teleporting
    private static final long TELEPORT_DELAY = 1000; // 1 second delay before teleporting
    private long startTime;
    private long appearTime;

    @Override
    public void init(Monster monster, GameState gameState) {
        // Nothing special on init; we haven't moved the hero yet.
        hasMovedHero = false;
        appearTime = monster.getAdjustedTime();
    }

    @Override
    public void update(Monster monster, GameState gameState) {
        long currentTime = monster.getAdjustedTime();

        // If we've already moved the hero, wait 2 seconds before removing
        if (hasMovedHero) {
            if (currentTime - startTime >= EXISTENCE_DURATION) {
                monster.markForRemoval();
            }
            return;
        }

        // Wait 1 second before teleporting the hero
        if (!hasMovedHero && currentTime - appearTime >= TELEPORT_DELAY) {
            moveHeroToRandomEmptySpot(gameState);
            hasMovedHero = true;
            startTime = currentTime;
        }
    }

    private void moveHeroToRandomEmptySpot(GameState gameState) {
        int[] spot = gameState.findRandomEmptyPosition();
        if (spot != null) {
            // Convert grid coordinates to pixels
            gameState.getHero().setPosition(spot[0], spot[1]);
        }
    }
}
