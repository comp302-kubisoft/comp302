/**
 * Manages the game's main menu interface. Handles menu rendering, option selection, and navigation
 * between different game modes. Features a wooden-themed UI with interactive menu options.
 */
package ui.menu;

import domain.model.GameMode;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import ui.sound.SoundManager;

public class Menu {
  /** Currently selected menu option index */
  private int selectedOption = 0;

  /** Available menu options */
  private final String[] options = { "Start Game", "Load", "Help", "Exit" };

  /** Time of last input processing */
  private long lastInputTime = 0;

  /** Minimum delay between input processing in milliseconds */
  private static final long INPUT_DELAY = 150;

  /** Dark background color for menu */
  private final Color BACKGROUND_DARK = new Color(72, 44, 52);

  /** Color for menu text */
  private final Color TEXT_COLOR = new Color(231, 231, 231);

  /** Color for selected menu option */
  private final Color SELECTED_COLOR = new Color(255, 255, 255);

  /** Color for unselected menu options */
  private final Color UNSELECTED_COLOR = new Color(180, 180, 180);

  /** Sound manager instance */
  private final SoundManager soundManager;

  private BufferedImage backgroundImage;

  public Menu(SoundManager soundManager) {
    this.selectedOption = 0;
    this.soundManager = soundManager;
    loadBackgroundImage();
  }

  private void loadBackgroundImage() {
    try {
      backgroundImage = ImageIO.read(getClass().getResourceAsStream("/maps/mainmenubackground.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Draws the menu interface. Renders the wooden panel background, title,
   * options, and control
   * hints.
   *
   * @param g2           Graphics context to draw with
   * @param screenWidth  Width of the game screen
   * @param screenHeight Height of the game screen
   */
  public void draw(Graphics2D g2, int screenWidth, int screenHeight) {
    // Draw the background image for the entire screen
    if (backgroundImage != null) {
        g2.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight, null);
    } else {
        g2.setColor(BACKGROUND_DARK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    // Draw title with shadow for better readability
    g2.setFont(new Font("Monospaced", Font.BOLD, 48));
    String title = "ROGUE-LIKE";
    int titleWidth = g2.getFontMetrics().stringWidth(title);
    
    // Draw shadow
    g2.setColor(new Color(0, 0, 0, 128));
    g2.drawString(title, screenWidth / 2 - titleWidth / 2 + 2, 150 + 2);
    
    // Draw main title
    g2.setColor(TEXT_COLOR);
    g2.drawString(title, screenWidth / 2 - titleWidth / 2, 150);

    // Draw menu options
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    int startY = screenHeight / 2 - 30;
    int spacing = 40;

    for (int i = 0; i < options.length; i++) {
        String text = options[i];
        if (i == selectedOption) {
            text = "[ " + text + " ]";
            // Draw shadow for selected option
            g2.setColor(new Color(0, 0, 0, 128));
            g2.drawString(text, screenWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2 + 1, 
                         startY + i * spacing + 1);
            g2.setColor(SELECTED_COLOR);
        } else {
            g2.setColor(UNSELECTED_COLOR);
        }
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, screenWidth / 2 - textWidth / 2, startY + i * spacing);
    }

    // Draw controls hint at bottom with shadow
    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
    String controls = "WASD/Arrows: Move    Enter: Select";
    int controlsWidth = g2.getFontMetrics().stringWidth(controls);
    
    // Draw shadow for controls
    g2.setColor(new Color(0, 0, 0, 128));
    g2.drawString(controls, screenWidth / 2 - controlsWidth / 2 + 1, 
                 screenHeight - 40 + 1);
    
    // Draw controls text
    g2.setColor(TEXT_COLOR);
    g2.drawString(controls, screenWidth / 2 - controlsWidth / 2, 
                 screenHeight - 40);
  }

  /**
   * Requires:
   * - upPressed, downPressed, enterPressed accurately reflect key press states at
   * the time of call.
   * 
   * Modifies:
   * - this.selectedOption (the currently highlighted menu option).
   * 
   * Effects:
   * 1. If upPressed is true and enough time has passed since last input:
   * - Decrement selectedOption (wrap around if needed) and play a cursor sound.
   * 2. If downPressed is true and enough time has passed since last input:
   * - Increment selectedOption (wrap around if needed) and play a cursor sound.
   * 3. If enterPressed is true and enough time has passed since last input:
   * - If currently selectedOption == 0 => Return GameMode.BUILD (start game)
   * - If selectedOption == 1 => Return GameMode.LOAD
   * - If selectedOption == 2 => Return GameMode.HELP
   * - If selectedOption == 3 => Call System.exit(0)
   * 4. If no option is selected by enterPress or if input is within delay, return
   * GameMode.MENU.
   */
  public GameMode handleInput(boolean upPressed, boolean downPressed, boolean enterPressed) {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastInputTime < INPUT_DELAY) {
      return GameMode.MENU;
    }

    if (upPressed) {
      selectedOption = (selectedOption - 1 + options.length) % options.length;
      soundManager.playSFX(6); // Play cursor sound
      lastInputTime = currentTime;
    }
    if (downPressed) {
      selectedOption = (selectedOption + 1) % options.length;
      soundManager.playSFX(6); // Play cursor sound
      lastInputTime = currentTime;
    }

    if (enterPressed) {
      switch (selectedOption) {
        case 0:
          return GameMode.BUILD;
        case 1:
          return GameMode.LOAD;
        case 2:
          return GameMode.HELP;
        case 3:
          System.exit(0);
          break;
      }
    }
    return GameMode.MENU;
  }
}
