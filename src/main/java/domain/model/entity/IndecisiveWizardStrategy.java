package domain.model.entity;

import domain.model.GameState;
import java.io.Serializable;

public class IndecisiveWizardStrategy implements WizardStrategy, Serializable {
    private static final long serialVersionUID = 1L;

    private static final long EXISTENCE_DURATION = 2000; // 2 seconds
    private long startTime;

    @Override
    public void init(Monster monster, GameState gameState) {
        startTime = monster.getAdjustedTime();
    }

    @Override
    public void update(Monster monster, GameState gameState) {
        long currentTime = monster.getAdjustedTime();
        if (currentTime - startTime >= EXISTENCE_DURATION) {
            // Mark the monster for removal after 2 seconds
            monster.markForRemoval();
        }
        // Otherwise, do nothing (indecisive).
    }
}