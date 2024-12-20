//Yuno test
package ui.ui_main;

import javax.swing.JFrame;

/** Entry point for the application. Sets up the main game window and initializes the game panel. */
public class Main {

  /**
   * Main method to start the application.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {

    // Create the main game window
    JFrame window = new JFrame();
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit application on close
    window.setResizable(false); // Prevent resizing to maintain aspect ratio
    window.setTitle("Rokue-Like"); // Set the window title

    // Create and add the game panel
    GamePanel gamePanel = new GamePanel();
    window.add(gamePanel);

    // Adjust the window size to fit the preferred size of the game panel
    window.pack();

    // Center the window on the screen
    window.setLocationRelativeTo(null);
    window.setVisible(true);

    // Start the game loop
    gamePanel.startGameThread();
  }
}
