/**
 * Maintains the current state of user input. This class tracks which keys are currently pressed,
 * providing a centralized way to access input state across the game.
 */
package ui.input;

public class InputState {
  /** Flag indicating if the up movement key is pressed */
  public boolean upPressed = false;

  /** Flag indicating if the down movement key is pressed */
  public boolean downPressed = false;

  /** Flag indicating if the left movement key is pressed */
  public boolean leftPressed = false;

  /** Flag indicating if the right movement key is pressed */
  public boolean rightPressed = false;

  /** Flag indicating if the enter/confirmation key is pressed */
  public boolean enterPressed = false;

  /** Flag indicating if the escape/cancel key is pressed */
  public boolean escapePressed = false;

  /** Flag indicating if the reveal key is pressed */
  public boolean revealPressed = false;

  /** Flag indicating if the cloak protection key is pressed */
  public boolean cloakPressed = false;

  /** Flag indicating if the luring gem key is pressed */
  public boolean luringGemPressed = false;

  /** Flag indicating if the throw gem is active */
  public boolean throwGemActive = false;

  /** Direction of the throw gem */
  public String throwDirection = null;

  /**
   * Resets all input states to their default (unpressed) state. Used when transitioning between
   * game modes or clearing input state.
   */
  public void reset() {
    upPressed = false;
    downPressed = false;
    leftPressed = false;
    rightPressed = false;
    enterPressed = false;
    escapePressed = false;
    revealPressed = false;
    cloakPressed = false;
    luringGemPressed = false;
    throwGemActive = false;
    throwDirection = null;
  }
}
