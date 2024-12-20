package ui.input;

public class InputState {
  public boolean upPressed = false;
  public boolean downPressed = false;
  public boolean leftPressed = false;
  public boolean rightPressed = false;

  public void reset() {
    upPressed = false;
    downPressed = false;
    leftPressed = false;
    rightPressed = false;
  }
}
