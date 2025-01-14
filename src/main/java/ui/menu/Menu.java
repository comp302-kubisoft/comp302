/**
 * Manages the game's main menu interface. Handles menu rendering, option selection, and navigation
 * between different game modes. Features a wooden-themed UI with interactive menu options.
 */
package ui.menu;

import domain.model.GameMode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
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

  /** Dark wood color for panel elements */
  private final Color WOOD_DARK = new Color(87, 61, 38);

  /** Light wood color for panel elements */
  private final Color WOOD_LIGHT = new Color(116, 82, 53);

  /** Color for menu text */
  private final Color TEXT_COLOR = new Color(231, 231, 231);

  /** Color for selected menu option */
  private final Color SELECTED_COLOR = new Color(255, 255, 255);

  /** Color for unselected menu options */
  private final Color UNSELECTED_COLOR = new Color(180, 180, 180);

  /** Sound manager instance */
  private final SoundManager soundManager;

  public Menu() {
    this.selectedOption = 0;
    this.soundManager = SoundManager.getInstance();
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
    g2.setColor(BACKGROUND_DARK);
    g2.fillRect(0, 0, screenWidth, screenHeight);

    drawWoodenPanel(g2, screenWidth, screenHeight);
    drawTitle(g2, screenWidth, screenHeight);
    drawOptions(g2, screenWidth, screenHeight);
    drawControlsHint(g2, screenWidth, screenHeight);
  }

  /**
   * Draws the wooden panel background with gradient and planks.
   *
   * @param g2           Graphics context to draw with
   * @param screenWidth  Width of the game screen
   * @param screenHeight Height of the game screen
   */
  private void drawWoodenPanel(Graphics2D g2, int screenWidth, int screenHeight) {
    int margin = 40;
    int panelWidth = screenWidth - 2 * margin;
    int panelHeight = screenHeight - 2 * margin;

    GradientPaint woodGradient = new GradientPaint(
        margin, margin, WOOD_DARK, margin + panelWidth, margin + panelHeight, WOOD_LIGHT);
    g2.setPaint(woodGradient);
    g2.fillRect(margin, margin, panelWidth, panelHeight);

    g2.setColor(WOOD_DARK);
    g2.setStroke(new BasicStroke(4));
    g2.drawRect(margin, margin, panelWidth, panelHeight);

    int plankHeight = 30;
    g2.setStroke(new BasicStroke(2));
    for (int y = margin + plankHeight; y < margin + panelHeight; y += plankHeight) {
      g2.drawLine(margin, y, margin + panelWidth, y);
    }
  }

  /**
   * Draws the menu title with shadow effect.
   *
   * @param g2           Graphics context to draw with
   * @param screenWidth  Width of the game screen
   * @param screenHeight Height of the game screen
   */
  private void drawTitle(Graphics2D g2, int screenWidth, int screenHeight) {
    String title = "ROGUE-LIKE";
    g2.setFont(new Font("Monospaced", Font.BOLD, 40));

    g2.setColor(BACKGROUND_DARK);
    g2.drawString(title, screenWidth / 2 - 120 + 2, screenHeight / 4 + 2);

    g2.setColor(TEXT_COLOR);
    g2.drawString(title, screenWidth / 2 - 120, screenHeight / 4);
  }

  /**
   * Draws the menu options with selection highlighting.
   *
   * @param g2           Graphics context to draw with
   * @param screenWidth  Width of the game screen
   * @param screenHeight Height of the game screen
   */
  private void drawOptions(Graphics2D g2, int screenWidth, int screenHeight) {
    int startY = screenHeight / 2 - 30;
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));

    for (int i = 0; i < options.length; i++) {
      if (i == selectedOption) {
        g2.setColor(SELECTED_COLOR);
        String text = "[ " + options[i] + " ]";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, screenWidth / 2 - textWidth / 2, startY + i * 40);
      } else {
        g2.setColor(UNSELECTED_COLOR);
        int textWidth = g2.getFontMetrics().stringWidth(options[i]);
        g2.drawString(options[i], screenWidth / 2 - textWidth / 2, startY + i * 40);
      }
    }
  }

  /**
   * Draws the controls hint box at the bottom of the menu.
   *
   * @param g2           Graphics context to draw with
   * @param screenWidth  Width of the game screen
   * @param screenHeight Height of the game screen
   */
  private void drawControlsHint(Graphics2D g2, int screenWidth, int screenHeight) {
    int hintY = screenHeight - 100;
    String controlsText = "WASD/Arrows: Move   Enter: Select";

    g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
    g2.setColor(TEXT_COLOR);

    int textWidth = g2.getFontMetrics().stringWidth(controlsText);
    int boxWidth = textWidth + 40;
    int boxHeight = 30;
    int boxX = screenWidth / 2 - boxWidth / 2;
    int boxY = hintY - 20;

    g2.setColor(WOOD_DARK);
    g2.fillRect(boxX, boxY, boxWidth, boxHeight);
    g2.setColor(WOOD_LIGHT);
    g2.setStroke(new BasicStroke(2));
    g2.drawRect(boxX, boxY, boxWidth, boxHeight);

    g2.setColor(TEXT_COLOR);
    g2.drawString(controlsText, screenWidth / 2 - textWidth / 2, hintY);
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
