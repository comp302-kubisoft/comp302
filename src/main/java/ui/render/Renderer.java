package ui.render;

import domain.model.GameMode;
import domain.model.GameState;
import domain.model.entity.Enchantment;
import domain.model.entity.Hero;
import domain.model.entity.Monster;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import ui.main.GamePanel;
import ui.menu.Menu;
import ui.tile.BuildObjectManager;

public class Renderer {

  private GameState gameState;
  private int tileSize;
  private int screenWidth;
  private int screenHeight;
  private Menu menu;
  private BuildObjectManager buildObjectManager;
  private boolean isPaused = false;
  private BufferedImage heartImage;
  private GamePanel gamePanel;

  private final Color BACKGROUND_DARK = new Color(72, 44, 52);
  private final Color WOOD_DARK = new Color(87, 61, 38);
  private final Color WOOD_LIGHT = new Color(116, 82, 53);
  private final Color TEXT_COLOR = new Color(231, 231, 231);

  // Hall theme colors
  private final Color EARTH_DARK = new Color(101, 67, 33); // Dark brown
  private final Color EARTH_LIGHT = new Color(140, 100, 60); // Light brown
  private final Color AIR_DARK = new Color(176, 196, 222); // Light steel blue
  private final Color AIR_LIGHT = new Color(230, 230, 250); // Lavender
  private final Color WATER_DARK = new Color(0, 105, 148); // Deep blue
  private final Color WATER_LIGHT = new Color(64, 164, 223); // Light blue
  private final Color FIRE_DARK = new Color(139, 0, 0); // Dark red
  private final Color FIRE_LIGHT = new Color(255, 69, 0); // Red-orange

  private Color getHallDarkColor(int hallNumber) {
    switch (hallNumber) {
      case 0:
        return EARTH_DARK;
      case 1:
        return AIR_DARK;
      case 2:
        return WATER_DARK;
      case 3:
        return FIRE_DARK;
      default:
        return WOOD_DARK;
    }
  }

  private Color getHallLightColor(int hallNumber) {
    switch (hallNumber) {
      case 0:
        return EARTH_LIGHT;
      case 1:
        return AIR_LIGHT;
      case 2:
        return WATER_LIGHT;
      case 3:
        return FIRE_LIGHT;
      default:
        return WOOD_LIGHT;
    }
  }

  private int selectedObjectIndex = -1; // -1 means no selection

  public Renderer(
      GameState gameState, int tileSize, int screenWidth, int screenHeight, GamePanel gamePanel) {
    this.gameState = gameState;
    this.tileSize = tileSize;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.buildObjectManager = new BuildObjectManager();
    this.gamePanel = gamePanel;
    loadHeartImage();
  }

  private void loadHeartImage() {
    try {
      heartImage = ImageIO.read(getClass().getResourceAsStream("/ui/heart.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setMenu(Menu menu) {
    this.menu = menu;
  }

  public void render(Graphics2D g2, GameMode currentMode) {
    switch (currentMode) {
      case MENU:
        if (menu != null) {
          menu.draw(g2, screenWidth, screenHeight);
        }
        break;
      case HELP:
        drawHelpScreen(g2);
        break;
      case GAME_OVER:
        drawGameOverScreen(g2);
        break;
      case VICTORY:
        drawVictoryScreen(g2);
        break;
      case PLAY:
        // Draw main game area
        gameState.getTileManager().draw(g2);

        // Draw placed objects first (behind the hero and monsters)
        for (GameState.PlacedObject obj : gameState.getPlacedObjects()) {
          g2.drawImage(
              buildObjectManager.getImage(obj.type), obj.x, obj.y, tileSize, tileSize, null);
        }

        // Draw enchantments
        for (Enchantment enchantment : gameState.getEnchantments()) {
          if (enchantment.isVisible()) {
            g2.drawImage(
                enchantment.getImage(),
                enchantment.getX(),
                enchantment.getY(),
                tileSize,
                tileSize,
                null);
          }
        }

        // Draw monsters
        for (Monster monster : gameState.getMonsters()) {
          g2.drawImage(
              monster.getImage(), monster.getX(), monster.getY(), tileSize, tileSize, null);
        }

        // Draw hero on top
        Hero hero = gameState.getHero();
        g2.drawImage(hero.getCurrentSprite(), hero.getX(), hero.getY(), tileSize, tileSize, null);

        // Draw right panel for UI elements
        drawPlayModePanel(g2);

        // Draw pause overlay if game is paused
        if (isPaused) {
          drawPauseOverlay(g2);
        }

        // Draw pause and cross buttons (always on top)
        drawPauseButton(g2);
        drawCrossButton(g2);

        drawLuringGem(g2);
        break;
      case BUILD:
        drawBuildMode(g2);
        break;
    }
  }

  public void render(Graphics2D g2, GameMode currentMode, Menu menu) {
    this.menu = menu;
    render(g2, currentMode);
  }

  public void setSelectedObject(int index) {
    this.selectedObjectIndex = index;
  }

  public int getSelectedObjectIndex() {
    return selectedObjectIndex;
  }

  private void drawBuildMode(Graphics2D g2) {
    // Draw the main game area
    gameState.getTileManager().draw(g2);

    // Draw placed objects
    for (GameState.PlacedObject obj : gameState.getPlacedObjects()) {
      g2.drawImage(buildObjectManager.getImage(obj.type), obj.x, obj.y, tileSize, tileSize, null);
    }

    // Draw the build panel on the right
    int panelMargin = 10;
    int panelWidth = screenWidth / 5;
    int panelX = screenWidth - panelWidth - panelMargin;
    int panelHeight = screenHeight - 2 * panelMargin;
    int panelY = panelMargin;

    // Get hall-specific colors
    Color darkColor = getHallDarkColor(gameState.getCurrentHall());
    Color lightColor = getHallLightColor(gameState.getCurrentHall());

    // Draw panel background with gradient
    GradientPaint woodGradient =
        new GradientPaint(
            panelX, panelY, darkColor, panelX + panelWidth, panelY + panelHeight, lightColor);
    g2.setPaint(woodGradient);
    g2.fillRect(panelX, panelY, panelWidth, panelHeight);

    // Draw panel border
    g2.setColor(darkColor);
    g2.setStroke(new BasicStroke(4));
    g2.drawRect(panelX, panelY, panelWidth, panelHeight);

    // Draw title
    g2.setFont(new Font("Monospaced", Font.BOLD, 20));
    g2.setColor(TEXT_COLOR);
    String title = "Build Mode";
    int titleWidth = g2.getFontMetrics().stringWidth(title);
    g2.drawString(title, panelX + (panelWidth - titleWidth) / 2, panelY + 30);

    // Draw current hall name
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    String hallText = getHallName(gameState.getCurrentHall());
    int hallWidth = g2.getFontMetrics().stringWidth(hallText);
    g2.drawString(hallText, panelX + (panelWidth - hallWidth) / 2, panelY + 60);

    // Draw object count and requirement
    g2.setFont(new Font("Monospaced", Font.BOLD, 14));
    int requiredObjects;
    switch (gameState.getCurrentHall()) {
      case 0:
        requiredObjects = 6;
        break;
      case 1:
        requiredObjects = 9;
        break;
      case 2:
        requiredObjects = 13;
        break;
      case 3:
        requiredObjects = 17;
        break;
      default:
        requiredObjects = 0;
    }
    int currentObjects = gameState.getPlacedObjects().size();
    String objectCountText = currentObjects + "/" + requiredObjects + " objects";
    int countWidth = g2.getFontMetrics().stringWidth(objectCountText);
    g2.setColor(
        currentObjects >= requiredObjects ? new Color(0, 255, 0) : new Color(255, 100, 100));
    g2.drawString(objectCountText, panelX + (panelWidth - countWidth) / 2, panelY + 80);

    // Draw navigation instructions
    g2.setColor(TEXT_COLOR);
    String navInstructions = "← → to switch halls";
    int navWidth = g2.getFontMetrics().stringWidth(navInstructions);
    g2.drawString(navInstructions, panelX + (panelWidth - navWidth) / 2, panelY + 100);

    // Draw action instructions
    String instructions =
        "Press ENTER to "
            + (gameState.getCurrentHall() < GameState.TOTAL_HALLS - 1 ? "save hall" : "start game");
    int instructionsWidth = g2.getFontMetrics().stringWidth(instructions);
    g2.drawString(instructions, panelX + (panelWidth - instructionsWidth) / 2, panelY + 120);

    // Draw right-click instruction
    String deleteInstruction = "Right click to delete";
    int deleteWidth = g2.getFontMetrics().stringWidth(deleteInstruction);
    g2.drawString(deleteInstruction, panelX + (panelWidth - deleteWidth) / 2, panelY + 140);

    // Draw object slots (adjusted Y position to accommodate new instruction)
    int slotMargin = 10;
    int slotSize = (panelWidth - 2 * slotMargin) / 2;
    int slotY = panelY + 160; // Increased from 140 to make room for new instruction
    int slotSpacing = slotSize + 15;

    g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
    int textMargin = 10;

    for (int i = 0; i < buildObjectManager.getObjectCount(); i++) {
      int currentSlotY = slotY + i * slotSpacing;

      // Draw selection highlight if this object is selected
      if (i == selectedObjectIndex) {
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRect(panelX + slotMargin - 2, currentSlotY - 2, slotSize + 4, slotSize + 4);
      }

      // Draw slot background
      g2.setColor(darkColor);
      g2.fillRect(panelX + slotMargin, currentSlotY, slotSize, slotSize);

      // Draw object image
      g2.drawImage(
          buildObjectManager.getImage(i),
          panelX + slotMargin,
          currentSlotY,
          slotSize,
          slotSize,
          null);

      // Draw object name
      g2.setColor(TEXT_COLOR);
      String objectName = buildObjectManager.getObjectName(i);
      g2.drawString(
          objectName,
          panelX + slotMargin + slotSize + textMargin,
          currentSlotY + slotSize / 2 + g2.getFontMetrics().getAscent() / 2);
    }

    // Draw cross button on top of everything
    drawCrossButton(g2);
  }

  private void drawHelpScreen(Graphics2D g2) {
    // Draw dark background
    g2.setColor(BACKGROUND_DARK);
    g2.fillRect(0, 0, screenWidth, screenHeight);

    // Draw main wooden panel
    int margin = 40;
    int panelWidth = screenWidth - 2 * margin;
    int panelHeight = screenHeight - 2 * margin;

    // Create gradient for wooden panel
    GradientPaint woodGradient =
        new GradientPaint(
            margin, margin, WOOD_DARK, margin + panelWidth, margin + panelHeight, WOOD_LIGHT);
    g2.setPaint(woodGradient);
    g2.fillRect(margin, margin, panelWidth, panelHeight);

    // Draw panel border
    g2.setColor(WOOD_DARK);
    g2.setStroke(new BasicStroke(4));
    g2.drawRect(margin, margin, panelWidth, panelHeight);

    // Draw title with shadow
    g2.setFont(new Font("Monospaced", Font.BOLD, 40));
    String title = "INSTRUCTIONS";
    int titleWidth = g2.getFontMetrics().stringWidth(title);
    // Draw shadow
    g2.setColor(new Color(0, 0, 0, 100));
    g2.drawString(title, screenWidth / 2 - titleWidth / 2 + 2, margin + 52);
    // Draw main title
    g2.setColor(TEXT_COLOR);
    g2.drawString(title, screenWidth / 2 - titleWidth / 2, margin + 50);

    // Calculate layout
    int contentStartY = margin + 100;
    int leftColumnX = margin + 60;
    int rightColumnX = screenWidth / 2 + 60;

    // Draw Game Objective Section at the top (full width)
    drawSection(g2, "Game Objective", leftColumnX, contentStartY);
    int objectiveY = contentStartY + 35;

    // Draw objective box with wooden background
    int boxPadding = 15;
    int boxWidth = screenWidth - 2 * (margin + boxPadding);
    int boxHeight = 120; // Reduced height
    g2.setColor(WOOD_DARK);
    g2.fillRect(leftColumnX - boxPadding, objectiveY - boxPadding, boxWidth, boxHeight);
    g2.setColor(WOOD_LIGHT);
    g2.drawRect(leftColumnX - boxPadding, objectiveY - boxPadding, boxWidth, boxHeight);

    // Draw objective text
    g2.setColor(TEXT_COLOR);
    g2.setFont(new Font("Monospaced", Font.PLAIN, 16));

    // Draw objectives more compactly
    g2.drawString(
        "1. Build four mystical halls by placing objects in each hall",
        leftColumnX,
        objectiveY + 15);
    g2.drawString(
        "2. Required objects:  Earth: 6  |  Air: 9  |  Water: 13  |  Fire: 17",
        leftColumnX,
        objectiveY + 40);
    g2.drawString(
        "3. Find the hidden rune in each hall while avoiding monsters",
        leftColumnX,
        objectiveY + 65);
    g2.drawString(
        "4. Once you find a rune, reach the exit door to proceed", leftColumnX, objectiveY + 90);

    // Start Y position for the next sections
    int nextSectionY = objectiveY + boxHeight + 20; // Reduced spacing

    // Draw Game Objects and Monsters sections
    drawSection(g2, "Game Objects", leftColumnX, nextSectionY);
    drawSection(g2, "Monsters", rightColumnX, nextSectionY);
    int itemsY = nextSectionY + 45; // Increased initial Y position

    // Draw object images and descriptions
    BufferedImage[] objects = {
      buildObjectManager.getImage(0), // chest
      buildObjectManager.getImage(1), // barrel
      buildObjectManager.getImage(2), // torch
      buildObjectManager.getImage(3), // skull
      buildObjectManager.getImage(4) // vase
    };

    // Draw objects with larger spacing
    int itemSpacing = 65; // Increased spacing between items
    for (int i = 0; i < objects.length; i++) {
      drawImageWithText(
          g2,
          objects[i],
          buildObjectManager.getObjectName(i),
          leftColumnX,
          itemsY + (i * itemSpacing),
          "May contain a hidden rune");
    }

    // Draw monster images and descriptions
    try {
      BufferedImage fighterImage =
          ImageIO.read(getClass().getResourceAsStream("/monsters/fighter.png"));
      BufferedImage archerImage =
          ImageIO.read(getClass().getResourceAsStream("/monsters/archer.png"));
      BufferedImage wizardImage =
          ImageIO.read(getClass().getResourceAsStream("/monsters/wizard.png"));

      drawImageWithText(
          g2, fighterImage, "Fighter", rightColumnX, itemsY, "Moves around and damages on contact");
      drawImageWithText(
          g2,
          archerImage,
          "Archer",
          rightColumnX,
          itemsY + itemSpacing,
          "Shoots arrows when hero is within 4 tiles");
      drawImageWithText(
          g2,
          wizardImage,
          "Wizard",
          rightColumnX,
          itemsY + (itemSpacing * 2),
          "Teleports runes between objects every 5 seconds");
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Adjust bottom sections position based on the content above
    int bottomSectionY = itemsY + (Math.max(objects.length, 3) * itemSpacing) + 40;

    // Draw Controls and Tips with more compact spacing
    drawSection(g2, "Controls", leftColumnX, bottomSectionY);
    drawSection(g2, "Tips", rightColumnX, bottomSectionY);
    g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
    g2.setColor(TEXT_COLOR);

    // Draw controls more compactly
    String[] controls = {
      "● WASD/Arrow Keys - Move the hero",
      "● Mouse Click - Check objects for runes",
      "● ESC - Return to menu / Pause game"
    };
    for (int i = 0; i < controls.length; i++) {
      g2.drawString(controls[i], leftColumnX, bottomSectionY + 25 + (i * 20));
    }

    // Draw tips more compactly
    String[] tips = {
      "● Stay close to objects to check them",
      "● Watch for Wizard's rune teleports",
      "● Keep track of checked objects"
    };
    for (int i = 0; i < tips.length; i++) {
      g2.drawString(tips[i], rightColumnX, bottomSectionY + 25 + (i * 20));
    }

    // Draw return instruction at bottom
    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
    String returnText = "Press ESC to return to menu";
    int returnWidth = g2.getFontMetrics().stringWidth(returnText);
    g2.drawString(returnText, screenWidth / 2 - returnWidth / 2, screenHeight - margin - 20);
  }

  // Helper method to draw section headers
  private void drawSection(Graphics2D g2, String title, int x, int y) {
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    g2.setColor(new Color(255, 215, 0)); // Gold color
    g2.drawString(title, x, y);
  }

  // Helper method to draw images with text
  private void drawImageWithText(
      Graphics2D g2, BufferedImage image, String title, int x, int y, String description) {
    int imageSize = 48;
    int textOffset = 60;

    // Draw image with wooden background
    g2.setColor(WOOD_DARK);
    g2.fillRect(x, y, imageSize, imageSize);
    g2.drawImage(image, x, y, imageSize, imageSize, null);
    g2.setColor(WOOD_LIGHT);
    g2.setStroke(new BasicStroke(1));
    g2.drawRect(x, y, imageSize, imageSize);

    // Draw title and description
    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
    g2.setColor(TEXT_COLOR);
    g2.drawString(title, x + textOffset, y + 20);

    g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
    g2.drawString(description, x + textOffset, y + 40);
  }

  private void drawCrossButton(Graphics2D g2) {
    int buttonSize = 30;
    int margin = 10;
    int x = screenWidth - buttonSize - margin;
    int y = margin;

    // Draw button background
    g2.setColor(WOOD_DARK);
    g2.fillRect(x, y, buttonSize, buttonSize);
    g2.setColor(WOOD_LIGHT);
    g2.setStroke(new BasicStroke(2));
    g2.drawRect(x, y, buttonSize, buttonSize);

    // Draw X
    g2.setColor(TEXT_COLOR);
    g2.setStroke(new BasicStroke(2));
    int padding = 8;
    g2.drawLine(x + padding, y + padding, x + buttonSize - padding, y + buttonSize - padding);
    g2.drawLine(x + buttonSize - padding, y + padding, x + padding, y + buttonSize - padding);
  }

  public boolean isWithinCrossButton(int mouseX, int mouseY) {
    int buttonSize = 30;
    int margin = 10;
    int x = screenWidth - buttonSize - margin;
    int y = margin;

    return mouseX >= x && mouseX <= x + buttonSize && mouseY >= y && mouseY <= y + buttonSize;
  }

  private void drawPauseButton(Graphics2D g2) {
    int buttonSize = 30;
    int margin = 10;
    int x = screenWidth - 2 * buttonSize - 2 * margin; // Position to the left of cross button
    int y = margin;

    // Draw button background
    g2.setColor(WOOD_DARK);
    g2.fillRect(x, y, buttonSize, buttonSize);
    g2.setColor(WOOD_LIGHT);
    g2.setStroke(new BasicStroke(2));
    g2.drawRect(x, y, buttonSize, buttonSize);

    // Draw pause/play symbol
    g2.setColor(TEXT_COLOR);
    g2.setStroke(new BasicStroke(2));
    int padding = 8;
    if (!isPaused) {
      // Draw pause symbol (two vertical lines)
      int lineWidth = 4;
      g2.fillRect(x + padding, y + padding, lineWidth, buttonSize - 2 * padding);
      g2.fillRect(
          x + buttonSize - padding - lineWidth, y + padding, lineWidth, buttonSize - 2 * padding);
    } else {
      // Draw play symbol (triangle)
      int[] xPoints = {x + padding, x + buttonSize - padding, x + padding};
      int[] yPoints = {y + padding, y + buttonSize / 2, y + buttonSize - padding};
      g2.fillPolygon(xPoints, yPoints, 3);
    }
  }

  public boolean isWithinPauseButton(int mouseX, int mouseY) {
    int buttonSize = 30;
    int margin = 10;
    int x = screenWidth - 2 * buttonSize - 2 * margin;
    int y = margin;

    return mouseX >= x && mouseX <= x + buttonSize && mouseY >= y && mouseY <= y + buttonSize;
  }

  public void togglePause() {
    isPaused = !isPaused;
    gamePanel.getGameController().handlePauseState(isPaused);
  }

  public boolean isPaused() {
    return isPaused;
  }

  private void drawPauseOverlay(Graphics2D g2) {
    // Store the original composite
    java.awt.Composite originalComposite = g2.getComposite();

    // Create a semi-transparent grey overlay
    Color overlayColor = new Color(128, 128, 128, 180); // Grey with alpha
    g2.setColor(overlayColor);

    // Use alpha composite for transparency
    g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));

    // Draw the overlay rectangle
    g2.fillRect(0, 0, screenWidth, screenHeight);

    // Draw "PAUSED" text
    g2.setColor(TEXT_COLOR);
    g2.setFont(new Font("Monospaced", Font.BOLD, 48));
    String pausedText = "PAUSED";
    int textWidth = g2.getFontMetrics().stringWidth(pausedText);
    int textHeight = g2.getFontMetrics().getHeight();
    g2.drawString(pausedText, screenWidth / 2 - textWidth / 2, screenHeight / 2 - textHeight / 2);

    // Restore the original composite
    g2.setComposite(originalComposite);
  }

  private void drawPlayModePanel(Graphics2D g2) {
    int panelMargin = 10;
    int panelWidth = screenWidth / 5;
    int panelX = screenWidth - panelWidth - panelMargin;
    int panelHeight = screenHeight - 2 * panelMargin;
    int panelY = panelMargin;

    // Get hall-specific colors
    Color darkColor = getHallDarkColor(gameState.getCurrentHall());
    Color lightColor = getHallLightColor(gameState.getCurrentHall());

    // Draw panel background with gradient
    GradientPaint woodGradient =
        new GradientPaint(
            panelX, panelY, darkColor, panelX + panelWidth, panelY + panelHeight, lightColor);
    g2.setPaint(woodGradient);
    g2.fillRect(panelX, panelY, panelWidth, panelHeight);

    // Draw panel border
    g2.setColor(darkColor);
    g2.setStroke(new BasicStroke(4));
    g2.drawRect(panelX, panelY, panelWidth, panelHeight);

    // Draw title
    g2.setFont(new Font("Monospaced", Font.BOLD, 20));
    g2.setColor(TEXT_COLOR);
    String title = "Status";
    int titleWidth = g2.getFontMetrics().stringWidth(title);
    g2.drawString(title, panelX + (panelWidth - titleWidth) / 2, panelY + 30);

    // Draw current hall name
    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
    String hallText = getHallName(gameState.getCurrentHall());
    int hallWidth = g2.getFontMetrics().stringWidth(hallText);
    g2.drawString(hallText, panelX + (panelWidth - hallWidth) / 2, panelY + 55);

    // Draw timer
    long timeRemaining = gameState.getTimeRemaining();
    int seconds = (int) (timeRemaining / 1000);
    int minutes = seconds / 60;
    seconds = seconds % 60;
    String timeText = String.format("%02d:%02d", minutes, seconds);

    // Draw timer with color based on remaining time
    if (seconds <= 10 && minutes == 0) {
      g2.setColor(Color.RED);
    }
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    int timeWidth = g2.getFontMetrics().stringWidth(timeText);
    g2.drawString(timeText, panelX + (panelWidth - timeWidth) / 2, panelY + 85);
    g2.setColor(TEXT_COLOR);

    // Draw hearts
    if (heartImage != null) {
      int heartSize = 30;
      int heartY = panelY + 105;
      int heartSpacing = 5;
      int totalHeartsWidth =
          (heartSize * gameState.getHero().getMaxHealth())
              + (heartSpacing * (gameState.getHero().getMaxHealth() - 1));
      int heartsStartX = panelX + (panelWidth - totalHeartsWidth) / 2;

      // Draw all heart containers (grayed out)
      for (int i = 0; i < gameState.getHero().getMaxHealth(); i++) {
        int heartX = heartsStartX + (heartSize + heartSpacing) * i;
        // Draw grayed out heart
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.drawImage(heartImage, heartX, heartY, heartSize, heartSize, null);
      }

      // Draw filled hearts for current health
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      for (int i = 0; i < gameState.getHero().getHealth(); i++) {
        int heartX = heartsStartX + (heartSize + heartSpacing) * i;
        g2.drawImage(heartImage, heartX, heartY, heartSize, heartSize, null);
      }
    }

    // Draw inventory section
    int inventoryY = panelY + 160;
    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
    String inventoryTitle = "Inventory";
    int invTitleWidth = g2.getFontMetrics().stringWidth(inventoryTitle);
    g2.drawString(inventoryTitle, panelX + (panelWidth - invTitleWidth) / 2, inventoryY);

    // Draw inventory slots
    int slotSize = 48;
    int slotSpacing = 10;
    int slotsStartX = panelX + (panelWidth - (3 * slotSize + 2 * slotSpacing)) / 2;
    int slotsY = inventoryY + 20;

    // Draw the three slots for storable enchantments
    Enchantment.Type[] storableTypes = {
      Enchantment.Type.REVEAL, Enchantment.Type.CLOAK_OF_PROTECTION, Enchantment.Type.LURING_GEM
    };

    // Define keystroke labels for each enchantment type
    String[] keyLabels = {"R", "P", "B"};

    for (int i = 0; i < storableTypes.length; i++) {
      int slotX = slotsStartX + i * (slotSize + slotSpacing);

      // Draw slot background
      g2.setColor(darkColor);
      g2.fillRect(slotX, slotsY, slotSize, slotSize);
      g2.setColor(lightColor);
      g2.setStroke(new BasicStroke(2));
      g2.drawRect(slotX, slotsY, slotSize, slotSize);

      // Get enchantment count
      int count = gameState.getEnchantmentCount(storableTypes[i]);

      // Draw enchantment image if we have any
      if (count > 0) {
        try {
          String imagePath =
              switch (storableTypes[i]) {
                case REVEAL -> "/enchantments/reveal.png";
                case CLOAK_OF_PROTECTION -> "/enchantments/cloak.png";
                case LURING_GEM -> "/enchantments/luring_gem.png";
                default -> null;
              };
          if (imagePath != null) {
            BufferedImage enchImage = ImageIO.read(getClass().getResourceAsStream(imagePath));
            g2.drawImage(enchImage, slotX, slotsY, slotSize, slotSize, null);

            // Draw count if greater than 1 (bottom right)
            if (count > 1) {
              g2.setColor(TEXT_COLOR);
              g2.setFont(new Font("Monospaced", Font.BOLD, 14));
              String countText = String.valueOf(count);
              g2.drawString(countText, slotX + slotSize - 15, slotsY + slotSize - 5);
            }

            // Draw keystroke label (top left)
            g2.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black background
            g2.fillRect(slotX + 2, slotsY + 2, 16, 16);
            g2.setColor(TEXT_COLOR);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.drawString(keyLabels[i], slotX + 5, slotsY + 14);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // Draw reveal effect if active
    if (gameState.isRevealEffectActive()) {
      int tileSize = gameState.getTileManager().getTileSize();
      int radius = tileSize * 3; // 6 tile diameter (adjust as needed)

      // Calculate center of reveal effect
      int centerX = (gameState.getRevealAreaX() + 2) * tileSize;
      int centerY = (gameState.getRevealAreaY() + 2) * tileSize;

      // Calculate fade based on remaining time
      float progress = gameState.getRevealProgress(); // 0.0 to 1.0
      int alpha = (int) (180 * (1.0 - progress)); // Fade from 180 to 0

      // Create a radial gradient for smooth circular effect
      RadialGradientPaint gradient =
          new RadialGradientPaint(
              centerX,
              centerY,
              radius,
              new float[] {0.0f, 0.7f, 1.0f},
              new Color[] {
                new Color(255, 255, 100, alpha), // Center: yellow
                new Color(255, 255, 100, alpha / 2), // Middle: semi-transparent
                new Color(255, 255, 100, 0) // Edge: fully transparent
              });

      // Draw the circular reveal effect
      g2.setPaint(gradient);
      g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
  }

  private void drawGameOverScreen(Graphics2D g2) {
    // Draw dark overlay
    g2.setColor(new Color(0, 0, 0, 200));
    g2.fillRect(0, 0, screenWidth, screenHeight);

    // Draw "GAME OVER" text
    g2.setColor(Color.RED);
    g2.setFont(new Font("Monospaced", Font.BOLD, 64));
    String gameOverText = "GAME OVER";
    int textWidth = g2.getFontMetrics().stringWidth(gameOverText);
    g2.drawString(gameOverText, screenWidth / 2 - textWidth / 2, screenHeight / 2);

    // Draw hint text
    g2.setColor(TEXT_COLOR);
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    String hintText = "Press ESC to return to menu";
    textWidth = g2.getFontMetrics().stringWidth(hintText);
    g2.drawString(hintText, screenWidth / 2 - textWidth / 2, screenHeight / 2 + 60);
  }

  private void drawVictoryScreen(Graphics2D g2) {
    // Draw dark overlay with golden tint
    g2.setColor(new Color(0, 0, 0, 200));
    g2.fillRect(0, 0, screenWidth, screenHeight);

    // Draw "VICTORY!" text
    g2.setColor(new Color(255, 215, 0)); // Gold color
    g2.setFont(new Font("Monospaced", Font.BOLD, 64));
    String victoryText = "VICTORY!";
    int textWidth = g2.getFontMetrics().stringWidth(victoryText);
    g2.drawString(victoryText, screenWidth / 2 - textWidth / 2, screenHeight / 2);

    // Draw congratulatory message
    g2.setColor(TEXT_COLOR);
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    String congratsText = "You have found all the mystical runes!";
    textWidth = g2.getFontMetrics().stringWidth(congratsText);
    g2.drawString(congratsText, screenWidth / 2 - textWidth / 2, screenHeight / 2 + 60);

    // Draw hint text
    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
    String hintText = "Press ESC to return to menu";
    textWidth = g2.getFontMetrics().stringWidth(hintText);
    g2.drawString(hintText, screenWidth / 2 - textWidth / 2, screenHeight / 2 + 120);
  }

  /** Sets the game state reference. Used when resetting the game state. */
  public void setGameState(GameState gameState) {
    this.gameState = gameState;
  }

  private String getHallName(int hallNumber) {
    switch (hallNumber) {
      case 0:
        return "Hall of Earth";
      case 1:
        return "Hall of Air";
      case 2:
        return "Hall of Water";
      case 3:
        return "Hall of Fire";
      default:
        return "Unknown Hall";
    }
  }

  private void drawLuringGem(Graphics2D g2) {
    if (gameState.isLuringGemActive()) {
      int tileSize = gameState.getTileManager().getTileSize();
      BufferedImage gemImage = gameState.getLuringGemImage();
      float fadeAlpha = gameState.getGemFadeAlpha();

      // Calculate current position based on throw progress
      float progress = gameState.getGemThrowProgress();
      int startX = gameState.getGemStartX();
      int startY = gameState.getGemStartY();
      int targetX = gameState.getGemTargetX();
      int targetY = gameState.getGemTargetY();

      // Use easing function for smooth movement
      float eased = 1 - (1 - progress) * (1 - progress);
      int currentX = (int) (startX + (targetX - startX) * eased);
      int currentY = (int) (startY + (targetY - startY) * eased);

      // Calculate glow effect intensity (pulsing)
      long currentTime = System.currentTimeMillis();
      float glowIntensity = (float) Math.abs(Math.sin(currentTime * 0.005)) * 0.5f + 0.5f;

      // Draw outer glow with fade
      int glowSize = (int) (tileSize * (1.2f + glowIntensity * 0.2f));
      int glowX = currentX + (tileSize - glowSize) / 2;
      int glowY = currentY + (tileSize - glowSize) / 2;

      // Create radial gradient for glow with fade
      RadialGradientPaint glow =
          new RadialGradientPaint(
              currentX + tileSize / 2,
              currentY + tileSize / 2,
              glowSize / 2,
              new float[] {0.0f, 0.5f, 1.0f},
              new Color[] {
                new Color(1f, 0.8f, 0.2f, 0.4f * glowIntensity * fadeAlpha), // Gold
                new Color(1f, 0.3f, 0.1f, 0.2f * glowIntensity * fadeAlpha), // Red
                new Color(1f, 1f, 1f, 0f) // Transparent
              });

      // Draw the glow
      g2.setPaint(glow);
      g2.fillOval(glowX, glowY, glowSize, glowSize);

      // Draw the gem image with fade
      if (gemImage != null) {
        // Set alpha composite for fading
        AlphaComposite alphaComposite =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha);
        g2.setComposite(alphaComposite);

        g2.drawImage(gemImage, currentX, currentY, tileSize, tileSize, null);

        // Add sparkle effects with fade
        Random random = new Random(currentTime / 100);
        for (int i = 0; i < 3; i++) {
          float sparkleX = currentX + random.nextFloat() * tileSize;
          float sparkleY = currentY + random.nextFloat() * tileSize;
          float sparkleSize = 2 + random.nextFloat() * 3;

          g2.setColor(new Color(1f, 1f, 1f, random.nextFloat() * 0.7f * fadeAlpha));
          g2.fillOval((int) sparkleX, (int) sparkleY, (int) sparkleSize, (int) sparkleSize);
        }

        // Reset composite to default
        g2.setComposite(AlphaComposite.SrcOver);
      }
    }
  }
}
