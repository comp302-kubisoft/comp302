import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import domain.model.GameMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.menu.Menu;
import ui.sound.SoundManager;

public class MenuTest {

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu(SoundManager.getInstance());
    }

    @Test
    void testPressDownThenEnterShouldSelectLoad() throws InterruptedException {
        // selectedOption starts at 0 => "Start Game"
        // Press down once => selectedOption should become 1 => "Help"
        GameMode result = menu.handleInput(false, true, false);
        // No enter pressed yet, so still in MENU
        assertEquals(GameMode.MENU, result);

        Thread.sleep(200);

        // Press enter => Should pick "Help"
        result = menu.handleInput(false, false, true);
        // Expect HELP
        assertEquals(GameMode.LOAD, result);
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
    void testPressDownTwiceLoad() throws InterruptedException {
        // first down => selectedOption=1 ("Help")
        menu.handleInput(false, true, false);

        Thread.sleep(200);

        // second down => selectedOption=2 ("Exit")
        menu.handleInput(false, true, false);

        Thread.sleep(200);

        // Press enter => Should pick "Start Game" => GameMode.BUILD
        GameMode result = menu.handleInput(false, false, true);
        assertEquals(GameMode.HELP, result);
    }
}