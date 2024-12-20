package ui.ui_main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles keyboard input for the game and updates the InputState.
 * Acts as the Controller in the MVC architecture.
 */
public class KeyHandler implements KeyListener {

    InputState inputState; // INPUTSTATE ADDED

    /** Constructor to initialize the KeyHandler with an InputState. */
    public KeyHandler(InputState inputState) { // CONSTRUCTOR UPDATED
        this.inputState = inputState;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used, but required by the KeyListener interface
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // MAP KEY PRESSES TO INPUTSTATE (WASD + ARROW KEYS)
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

        // MAP KEY RELEASES TO INPUTSTATE (WASD + ARROW KEYS)
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
