package ui.menu;

import static org.junit.jupiter.api.Assertions.*;

import domain.model.GameMode;
import org.junit.jupiter.api.Test;
import ui.sound.SoundManager;

class MenuTest {

  private Menu createMenu() {
    return new Menu(SoundManager.getInstance());
  }

  @Test
  void handleInput_NoInput_ReturnsMenuMode() {
    Menu menu = createMenu();
    assertEquals(GameMode.MENU, menu.handleInput(false, false, false));
  }

  // ... rest of your test methods, but use createMenu() instead of new Menu()
}
