package domain.model.entity;

import domain.model.GameState;

/**
 * A strategy interface for handling the Wizard monster's behavior.
 */
public interface WizardStrategy {

    /**
     * Called once when the Wizard monster object is first created (or when
     * it switches to this strategy).
     *
     * @param monster   The monster implementing this strategy
     * @param gameState The current game state
     */
    void init(Monster monster, GameState gameState);

    /**
     * Called on each update cycle. Implementers may adjust the Wizard's
     * behavior according to the game state (e.g., teleporting runes,
     * moving the hero, disappearing, etc.).
     *
     * @param monster   The monster implementing this strategy
     * @param gameState The current game state
     */
    void update(Monster monster, GameState gameState);
}