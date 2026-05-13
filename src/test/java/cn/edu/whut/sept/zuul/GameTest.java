package cn.edu.whut.sept.zuul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * 游戏扩展功能测试。
 */
class GameTest {
    @Test
    void shouldHandleKnownAndUnknownCommands() {
        Game game = new Game(new Random(1));

        assertFalse(game.processCommand(new Command("help", null)));
        assertFalse(game.processCommand(new Command(null, null)));
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
}
