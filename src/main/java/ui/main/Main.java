package ui.main;

import javax.swing.JFrame;
import ui.menu.Menu;

public class Main {

  public static void main(String[] args) {
    JFrame window = new JFrame();
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setResizable(false);
    window.setTitle("Rogue-Like");

    GamePanel gamePanel = new GamePanel();
    
    window.add(gamePanel);
    window.pack();
    window.setLocationRelativeTo(null);
    window.setVisible(true);

    gamePanel.startGameThread();
  }
}
