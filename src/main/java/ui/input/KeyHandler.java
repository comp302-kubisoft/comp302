package ui.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

  InputState inputState;

  public KeyHandler(InputState inputState) {
    this.inputState = inputState;
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
      inputState.upPressed = true;
    }
    if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
      inputState.downPressed = true;
    }
    if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
      inputState.leftPressed = true;
    }
    if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
      inputState.rightPressed = true;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
      inputState.upPressed = false;
    }
    if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
      inputState.downPressed = false;
    }
    if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
      inputState.leftPressed = false;
    }
    if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
      inputState.rightPressed = false;
    }
  }
}
