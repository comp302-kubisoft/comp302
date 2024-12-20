package ui.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public InputState inputState;

    public KeyHandler(InputState inputState) {
        this.inputState = inputState;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch(code) {
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
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch(code) {
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
        }
    }
}