/**
 * Handles keyboard input events for the game.
 * This class listens for keyboard events and updates the InputState accordingly.
 * Supports both WASD and arrow key controls.
 */
package ui.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    /** Reference to the shared input state */
    public InputState inputState;

    /**
     * Creates a new KeyHandler with a reference to the input state.
     * 
     * @param inputState The shared input state to update based on key events
     */
    public KeyHandler(InputState inputState) {
        this.inputState = inputState;
    }

    /**
     * Required by KeyListener interface but not used.
     * 
     * @param e The key event
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Handles key press events.
     * Updates the input state when movement or action keys are pressed.
     * Supports both WASD and arrow keys for movement.
     * 
     * @param e The key press event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                inputState.upPressed = true;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                inputState.downPressed = true;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                inputState.leftPressed = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                inputState.rightPressed = true;
                break;
            case KeyEvent.VK_ENTER:
                inputState.enterPressed = true;
                break;
            case KeyEvent.VK_ESCAPE:
                inputState.escapePressed = true;
                break;
            case KeyEvent.VK_R:
                inputState.revealPressed = true;
                break;
            case KeyEvent.VK_P:
                inputState.cloakPressed = true;
                break;
            case KeyEvent.VK_B:
                inputState.luringGemPressed = true;
                break;
        }
        
        if (inputState.throwGemActive) {
            switch (code) {
                case KeyEvent.VK_A, KeyEvent.VK_LEFT -> inputState.throwDirection = "left";
                case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> inputState.throwDirection = "right";
                case KeyEvent.VK_W, KeyEvent.VK_UP -> inputState.throwDirection = "up";
                case KeyEvent.VK_S, KeyEvent.VK_DOWN -> inputState.throwDirection = "down";
            }
        }
    }

    /**
     * Handles key release events.
     * Updates the input state when movement or action keys are released.
     * Supports both WASD and arrow keys for movement.
     * 
     * @param e The key release event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                inputState.upPressed = false;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                inputState.downPressed = false;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                inputState.leftPressed = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                inputState.rightPressed = false;
                break;
            case KeyEvent.VK_ENTER:
                inputState.enterPressed = false;
                break;
            case KeyEvent.VK_ESCAPE:
                inputState.escapePressed = false;
                break;
            case KeyEvent.VK_R:
                inputState.revealPressed = false;
                break;
            case KeyEvent.VK_P:
                inputState.cloakPressed = false;
                break;
        }
    }
}