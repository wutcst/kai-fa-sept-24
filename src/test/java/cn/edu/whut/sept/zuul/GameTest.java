package cn.edu.whut.sept.zuul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

/**
 * 游戏扩展功能测试。
 */
class GameTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldHandleKnownAndUnknownCommands() {
        Game game = new Game(new Random(1));

        assertFalse(game.processCommand(new Command("help", null)));
        assertFalse(game.processCommand(new Command(null, null)));
        assertFalse(game.processCommand(new Command("quest", null)));
        assertFalse(game.processCommand(new Command("score", null)));
        assertFalse(game.processCommand(new Command("status", null)));
        assertFalse(game.processCommand(new Command("map", null)));
        assertTrue(game.processCommand(new Command("quit", null)));
        assertFalse(game.processCommand(new Command("quit", "now")));
    }

    @Test
    void shouldMoveAndRejectInvalidDirection() {
        Game game = new Game(new Random(1));

        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("outside"));
        game.processCommand(new Command("go", "east"));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("lecture theater"));
        game.processCommand(new Command("go", "north"));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("lecture theater"));
    }

    @Test
    void shouldShowRoomItemsWithLook() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "east"));
        assertNotNull(game.getPlayer().getCurrentRoom().getItem("book"));
        assertTrue(game.getPlayer().getCurrentRoom().getLongDescription().contains("book"));
        assertFalse(game.processCommand(new Command("look", null)));
    }

    @Test
    void shouldBackThroughRoomHistory() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("back", null));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("outside"));
        game.processCommand(new Command("back", null));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("outside"));
    }

    @Test
    void shouldTakeDropAndDropAllItems() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("take", "book"));
        assertNotNull(game.getPlayer().getItem("book"));
        assertNull(game.getPlayer().getCurrentRoom().getItem("book"));

        game.processCommand(new Command("drop", "book"));
        assertNull(game.getPlayer().getItem("book"));
        assertNotNull(game.getPlayer().getCurrentRoom().getItem("book"));

        game.processCommand(new Command("take", "book"));
        game.processCommand(new Command("drop", "all"));
        assertNull(game.getPlayer().getItem("book"));
        assertNotNull(game.getPlayer().getCurrentRoom().getItem("book"));
        assertFalse(game.processCommand(new Command("items", null)));
    }

    @Test
    void shouldKeepTooHeavyItemInRoom() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("take", "stone"));
        assertNull(game.getPlayer().getItem("stone"));
        assertNotNull(game.getPlayer().getCurrentRoom().getItem("stone"));
    }

    @Test
    void shouldIncreaseCarryWeightAfterEatingCookie() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "south"));
        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("eat", "cookie"));

        assertEquals(10, game.getPlayer().getMaxCarryWeight());
        assertNull(game.getPlayer().getCurrentRoom().getItem("cookie"));

        game.processCommand(new Command("back", null));
        game.processCommand(new Command("back", null));
        game.processCommand(new Command("take", "stone"));
        assertNotNull(game.getPlayer().getItem("stone"));
    }

    @Test
    void shouldTransportPlayerWhenEnteringPortalRoom() {
        Game game = new Game(new Random(2));

        game.processCommand(new Command("go", "north"));

        assertNotEquals(game.getPortal(), game.getPlayer().getCurrentRoom());
        assertNotNull(game.getPlayer().getCurrentRoom());
    }

    @Test
    void shouldSupportDirectionAndCommandAliases() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("e", null));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("lecture theater"));
        game.processCommand(new Command("get", "book"));
        assertNotNull(game.getPlayer().getItem("book"));
    }

    @Test
    void shouldRequireKeyBeforeEnteringArchive() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "south"));
        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("go", "east"));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("admin office"));

        game.processCommand(new Command("back", null));
        game.processCommand(new Command("back", null));
        game.processCommand(new Command("go", "west"));
        game.processCommand(new Command("take", "key"));
        game.processCommand(new Command("back", null));
        game.processCommand(new Command("go", "south"));
        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("go", "east"));
        assertTrue(game.getPlayer().getCurrentRoom().getShortDescription().contains("project archive"));
        assertTrue(game.getPlayer().getScore() >= 11);
    }

    @Test
    void shouldWinAfterTakingTreasureBackToEntrance() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "west"));
        game.processCommand(new Command("take", "key"));
        game.processCommand(new Command("back", null));
        game.processCommand(new Command("go", "south"));
        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("go", "east"));
        game.processCommand(new Command("take", "treasure"));
        game.processCommand(new Command("back", null));
        game.processCommand(new Command("back", null));
        boolean finished = game.processCommand(new Command("back", null));

        assertTrue(finished);
        assertTrue(game.getPlayer().hasItem("treasure"));
        assertTrue(game.getPlayer().getScore() >= 50);
    }

    @Test
    void shouldLoseHealthInDangerousRoom() {
        Game game = new Game(new Random(1));

        game.processCommand(new Command("go", "south"));

        assertEquals(8, game.getPlayer().getHealth());
    }

    @Test
    void shouldSaveAndLoadGame() {
        String oldUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            Game savedGame = new Game(new Random(1));
            savedGame.processCommand(new Command("go", "west"));
            savedGame.processCommand(new Command("take", "key"));
            savedGame.processCommand(new Command("save", null));

            Game loadedGame = new Game(new Random(1));
            loadedGame.processCommand(new Command("load", null));

            assertTrue(loadedGame.getPlayer().getCurrentRoom().getShortDescription().contains("campus pub"));
            assertTrue(loadedGame.getPlayer().hasItem("key"));
            assertNull(loadedGame.getPlayer().getCurrentRoom().getItem("key"));
        }
        finally {
            System.setProperty("user.dir", oldUserDir);
        }
    }
}
