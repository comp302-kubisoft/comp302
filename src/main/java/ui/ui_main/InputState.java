package ui.ui_main;

/**
 * Represents the state of user inputs at any given time.
 * Acts as a centralized input data store.
 */
public class InputState {

    public boolean upPressed = false;
    public boolean downPressed = false;
    public boolean leftPressed = false;
    public boolean rightPressed = false;

    /** Resets all input states to false. */
    public void reset() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
    }
}
