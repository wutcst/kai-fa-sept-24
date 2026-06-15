package cn.edu.whut.sept.dungeon.core;

import cn.edu.whut.sept.dungeon.entity.Item;
import cn.edu.whut.sept.dungeon.entity.Npc;
import cn.edu.whut.sept.dungeon.world.Position;
import cn.edu.whut.sept.dungeon.world.World;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreGameplayFlowTest {
    private static final long SEED = 20260614L;

    @Test
    public void replayInputCanCompleteCoreGameplayLoop() {
        String replayInput = buildWinningReplayInput(SEED);

        GameState completed = new GameEngine().playWithInputString(replayInput).getState();

        assertTrue(completed.isCompleted());
        assertTrue(completed.getQuest().isReportIssued());
        assertTrue(completed.getQuest().isMavenPuzzleSolved());
        assertTrue(completed.getQuest().isSlidesExported());
        assertTrue(completed.getQuest().isPassIssued());
        assertTrue(completed.getInventory().containsAll("report", "laptop", "slides", "pass"));
        assertTrue(completed.getMessage().contains("Defense completed"));
    }

    @Test
    public void wrongMavenPuzzleAnswerKeepsAssistantMaterialsLocked() {
        GameEngine engine = new GameEngine();
        engine.handleInput(InputCommand.newGame(SEED));

        moveTo(engine, findItem(engine.getState(), "usb").getPosition());
        engine.handleInput(InputCommand.fromKey('e'));
        moveTo(engine, findNpc(engine.getState(), "assistant").getPosition());
        GameState prompt = engine.handleInput(InputCommand.fromKey('e'));
        GameState wrongAnswer = engine.handleInput(InputCommand.answer("build.gradle"));
        GameState stillLocked = engine.handleInput(InputCommand.fromKey('e'));

        assertTrue(prompt.getMessage().contains("Maven"));
        assertFalse(wrongAnswer.getQuest().isMavenPuzzleSolved());
        assertFalse(stillLocked.getInventory().contains("laptop"));
        assertFalse(stillLocked.getInventory().contains("slides"));
        assertTrue(stillLocked.getMessage().contains("Maven"));
    }

    @Test
    public void defenseHallRejectsPartialCoreQuestProgress() {
        GameEngine engine = new GameEngine();
        engine.handleInput(InputCommand.newGame(SEED));

        moveTo(engine, findItem(engine.getState(), "student-card").getPosition());
        engine.handleInput(InputCommand.fromKey('e'));
        moveTo(engine, findNpc(engine.getState(), "librarian").getPosition());
        engine.handleInput(InputCommand.fromKey('e'));
        moveTo(engine, engine.getState().getWorld().getDefenseHallPosition());
        GameState rejected = engine.handleInput(InputCommand.fromKey('e'));

        assertTrue(rejected.getInventory().contains("report"));
        assertFalse(rejected.isCompleted());
        assertTrue(rejected.getMessage().contains("Defense hall locked"));
        assertTrue(rejected.getMessage().contains("laptop"));
        assertTrue(rejected.getMessage().contains("slides"));
        assertTrue(rejected.getMessage().contains("pass"));
    }

    private String buildWinningReplayInput(long seed) {
        GameEngine builder = new GameEngine();
        StringBuilder input = new StringBuilder("n").append(seed).append("s");
        builder.handleInput(InputCommand.newGame(seed));

        appendMoveAndInteract(input, builder, findItem(builder.getState(), "student-card").getPosition());
        appendMoveAndInteract(input, builder, findNpc(builder.getState(), "librarian").getPosition());
        appendMoveAndInteract(input, builder, findItem(builder.getState(), "usb").getPosition());
        appendMoveAndInteract(input, builder, findNpc(builder.getState(), "assistant").getPosition());

        input.append("!answer(pom.xml)");
        builder.handleInput(InputCommand.answer("pom.xml"));
        input.append('e');
        builder.handleInput(InputCommand.fromKey('e'));

        appendMoveAndInteract(input, builder, findNpc(builder.getState(), "teacher").getPosition());
        appendMoveAndInteract(input, builder, builder.getState().getWorld().getDefenseHallPosition());
        return input.toString();
    }

    private void appendMoveAndInteract(StringBuilder input, GameEngine engine, Position target) {
        String path = pathTo(engine.getState(), target);
        input.append(path).append('e');
        for (int i = 0; i < path.length(); i++) {
            engine.handleInput(InputCommand.fromKey(path.charAt(i)));
        }
        engine.handleInput(InputCommand.fromKey('e'));
    }

    private void moveTo(GameEngine engine, Position target) {
        String path = pathTo(engine.getState(), target);
        for (int i = 0; i < path.length(); i++) {
            engine.handleInput(InputCommand.fromKey(path.charAt(i)));
        }
    }

    private Item findItem(GameState state, String itemId) {
        for (Item item : state.getItems()) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        throw new AssertionError("Missing item: " + itemId);
    }

    private Npc findNpc(GameState state, String npcId) {
        for (Npc npc : state.getNpcs()) {
            if (npc.getId().equals(npcId)) {
                return npc;
            }
        }
        throw new AssertionError("Missing npc: " + npcId);
    }

    private String pathTo(GameState state, Position target) {
        World world = state.getWorld();
        Position start = state.getPlayer().getPosition();
        boolean[][] visited = new boolean[world.getHeight()][world.getWidth()];
        Queue<PathNode> queue = new ArrayDeque<PathNode>();
        queue.add(new PathNode(start, ""));
        visited[start.getY()][start.getX()] = true;

        while (!queue.isEmpty()) {
            PathNode current = queue.remove();
            if (current.position.equals(target)) {
                return current.path;
            }
            Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            for (Direction direction : directions) {
                Position next = new Position(current.position.getX() + direction.getDx(),
                        current.position.getY() + direction.getDy());
                if (world.contains(next.getX(), next.getY())
                        && !visited[next.getY()][next.getX()]
                        && world.isWalkable(next)) {
                    visited[next.getY()][next.getX()] = true;
                    queue.add(new PathNode(next, current.path + keyFor(direction)));
                }
            }
        }
        throw new AssertionError("Could not find path to " + target);
    }

    private char keyFor(Direction direction) {
        switch (direction) {
            case NORTH:
                return 'w';
            case SOUTH:
                return 's';
            case WEST:
                return 'a';
            case EAST:
                return 'd';
            default:
                throw new IllegalArgumentException("Unsupported direction: " + direction);
        }
    }

    private static final class PathNode {
        private final Position position;
        private final String path;

        private PathNode(Position position, String path) {
            this.position = position;
            this.path = path;
        }
    }
}
