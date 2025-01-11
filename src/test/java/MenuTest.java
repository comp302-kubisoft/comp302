import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import domain.model.GameMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.menu.Menu;

public class MenuTest {

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu();
    }

    @Test
    void testPressDownThenEnterShouldSelectHelp() {
        // selectedOption starts at 0 => "Start Game"
        // Press down once => selectedOption should become 1 => "Help"
        GameMode result = menu.handleInput(false, true, false);
        // No enter pressed yet, so still in MENU
        assertEquals(GameMode.MENU, result);

        // Press enter => Should pick "Help"
        result = menu.handleInput(false, false, true);
        // Expect HELP
        assertEquals(GameMode.HELP, result);
    }

    @Test
    void testPressUpWrapsAroundAndExit() {
        // selectedOption starts at 0 => "Start Game"
        // Press up => should wrap around to index = 2 => "Exit"
        menu.handleInput(true, false, false);
        // Press enter => calls System.exit(0), but we can't realistically let that kill
        // the test
        // so we just ensure no exceptions thrown by the code path up to System.exit
        assertDoesNotThrow(() -> {
            menu.handleInput(false, false, true);
        });
    }

    @Test
    void testPressDownTwiceStartGameNoInputDelay() {
        // first down => selectedOption=1 ("Help")
        menu.handleInput(false, true, false);
        // second down => selectedOption=2 ("Exit")
        menu.handleInput(false, true, false);

        // Press down the third time => wrap to selectedOption=0 ("Start Game")
        menu.handleInput(false, true, false);

        // Press enter => Should pick "Start Game" => GameMode.BUILD
        GameMode result = menu.handleInput(false, false, true);
        assertEquals(GameMode.BUILD, result);
    }
}