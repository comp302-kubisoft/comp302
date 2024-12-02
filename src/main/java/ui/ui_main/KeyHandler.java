package ui.ui_main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles keyboard input for the game, mapping key presses and releases to
 * movement actions.
 */
public class KeyHandler implements KeyListener {

  /**
   * Flags for directional input.
   */
  public boolean upPressed, downPressed, leftPressed, rightPressed;

  @Override
  public void keyTyped(KeyEvent e) {
    // Not used, but required by the KeyListener interface
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();

    // Map key presses to movement directions
    if (code == KeyEvent.VK_W) {
      upPressed = true;
    }
    if (code == KeyEvent.VK_S) {
      downPressed = true;
    }
    if (code == KeyEvent.VK_A) {
      leftPressed = true;
    }
    if (code == KeyEvent.VK_D) {
      rightPressed = true;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    int code = e.getKeyCode();

    // Map key releases to stop movement in the corresponding direction
    if (code == KeyEvent.VK_W) {
      upPressed = false;
    }
    if (code == KeyEvent.VK_S) {
      downPressed = false;
    }
    if (code == KeyEvent.VK_A) {
      leftPressed = false;
    }
    if (code == KeyEvent.VK_D) {
      rightPressed = false;
    }
  }
}
