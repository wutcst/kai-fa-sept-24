package cn.edu.whut.sept.dungeon.core;

import cn.edu.whut.sept.dungeon.entity.Inventory;
import cn.edu.whut.sept.dungeon.entity.Item;
import cn.edu.whut.sept.dungeon.world.Position;
import cn.edu.whut.sept.dungeon.world.Room;
import cn.edu.whut.sept.dungeon.world.World;
import cn.edu.whut.sept.dungeon.world.WorldGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameState {
    public static final int VISION_RADIUS = 5;
    private static final String REPORT = "report";
    private static final String LAPTOP = "laptop";
    private static final String SLIDES = "slides";
    private static final String PASS = "pass";
    private static final String STUDENT_CARD = "student-card";
    private static final String USB = "usb";

    private final Long seed;
    private final boolean started;
    private final boolean exited;
    private final boolean saveRequested;
    private final PlayerState player;
    private final World world;
    private final Inventory inventory;
    private final List<Item> items;
    private final boolean completed;
    private final boolean[][] explored;
    private final boolean[][] visible;
    private final String message;

    private GameState(Long seed, boolean started, boolean exited, boolean saveRequested,
                      PlayerState player, World world, Inventory inventory, List<Item> items, boolean completed,
                      boolean[][] explored, boolean[][] visible, String message) {
        this.seed = seed;
        this.started = started;
        this.exited = exited;
        this.saveRequested = saveRequested;
        this.player = player;
        this.world = world;
        this.inventory = inventory == null ? Inventory.empty() : inventory;
        this.items = Collections.unmodifiableList(new ArrayList<Item>(items == null
                ? Collections.<Item>emptyList()
                : items));
        this.completed = completed;
        this.explored = copyGrid(explored);
        this.visible = copyGrid(visible);
        this.message = message;
    }

    public static GameState initial() {
        return new GameState(null, false, false, false, PlayerState.origin(),
                null, Inventory.empty(), Collections.<Item>emptyList(), false, null, null, "Ready.");
    }

    public static GameState newGame(long seed) {
        World world = new WorldGenerator().generate(seed);
        PlayerState player = PlayerState.at(world.getSpawnPosition());
        return new GameState(seed, true, false, false, player, world,
                Inventory.empty(), createItems(world), false,
                createExploredFor(world, player), createVisibleFor(world, player),
                "New game started with seed " + seed + ".");
    }

    public static GameState restored(Long seed, boolean started, boolean exited, boolean saveRequested,
                                     PlayerState player, World world, Inventory inventory, List<Item> items,
                                     boolean completed, boolean[][] explored, String message) {
        return new GameState(seed, started, exited, saveRequested, player, world,
                inventory, items, completed, explored, world == null ? null : createVisibleFor(world, player),
                message);
    }

    public Long getSeed() {
        return seed;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isExited() {
        return exited;
    }

    public boolean isSaveRequested() {
        return saveRequested;
    }

    public PlayerState getPlayer() {
        return player;
    }

    public String getWorldStatus() {
        return world == null ? "not-created" : "generated";
    }

    public World getWorld() {
        return world;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Item> getItems() {
        return items;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isExplored(int x, int y) {
        return isMarked(explored, x, y);
    }

    public boolean isVisible(int x, int y) {
        return isMarked(visible, x, y);
    }

    public VisibilityState getVisibilityState(int x, int y) {
        if (isVisible(x, y)) {
            return VisibilityState.VISIBLE;
        }
        if (isExplored(x, y)) {
            return VisibilityState.SEEN;
        }
        return VisibilityState.UNSEEN;
    }

    public boolean[][] copyExplored() {
        return copyGrid(explored);
    }

    public int getVisibleCount() {
        return countMarked(visible);
    }

    public int getExploredCount() {
        return countMarked(explored);
    }

    public String getMessage() {
        return message;
    }

    public GameState withMessage(String nextMessage) {
        return new GameState(seed, started, exited, saveRequested, player, world,
                inventory, items, completed, explored, visible, nextMessage);
    }

    public GameState markExited() {
        return new GameState(seed, started, true, saveRequested, player, world,
                inventory, items, completed, explored, visible, message);
    }

    public GameState markSaveRequested() {
        return new GameState(seed, started, exited, true, player, world,
                inventory, items, completed, explored, visible, message);
    }

    public GameState movePlayer(Direction direction) {
        if (!started || world == null || direction == null) {
            return withMessage("Start a new game first.");
        }

        PlayerState turnedPlayer = player.withDirection(direction);
        Position target = new Position(player.getX() + direction.getDx(), player.getY() + direction.getDy());
        if (!world.isWalkable(target)) {
            return new GameState(seed, started, exited, saveRequested, turnedPlayer, world,
                    inventory, items, completed, explored, visible, "Blocked by wall.");
        }

        PlayerState movedPlayer = turnedPlayer.moveTo(target);
        return new GameState(seed, started, exited, saveRequested, movedPlayer, world,
                inventory, items, completed,
                createExploredFor(world, movedPlayer, explored), createVisibleFor(world, movedPlayer),
                "Moved " + direction.name() + ".");
    }

    public GameState interact() {
        if (!started || world == null) {
            return withMessage("Start a new game first.");
        }

        if (player.getPosition().equals(world.getDefenseHallPosition())) {
            if (inventory.containsAll(REPORT, LAPTOP, SLIDES, PASS)) {
                return new GameState(seed, started, exited, saveRequested, player, world,
                        inventory, items, true, explored, visible,
                        "Defense completed in " + player.getSteps() + " steps. Excellent software engineering practice!");
            }
            return withMessage("Defense hall locked. Missing: " + missingDefenseMaterials() + ".");
        }
        Item item = itemAt(player.getPosition());
        if (item != null) {
            return collectItem(item);
        }
        return withMessage("Nothing to interact with here.");
    }

    public GameState describeInventory() {
        return withMessage("Inventory: " + inventory.summary() + ".");
    }

    public Item itemAt(Position position) {
        for (Item item : items) {
            if (!item.isCollected() && item.getPosition().equals(position)) {
                return item;
            }
        }
        return null;
    }

    private GameState collectItem(Item item) {
        List<Item> nextItems = new ArrayList<Item>();
        for (Item current : items) {
            nextItems.add(current.getId().equals(item.getId()) ? current.collect() : current);
        }
        Inventory nextInventory = inventory.add(item.getId());
        return new GameState(seed, started, exited, saveRequested, player, world,
                nextInventory, nextItems, completed, explored, visible,
                "Picked up " + item.getName() + ".");
    }

    private String missingDefenseMaterials() {
        List<String> missing = new ArrayList<String>();
        addMissing(missing, REPORT);
        addMissing(missing, LAPTOP);
        addMissing(missing, SLIDES);
        addMissing(missing, PASS);
        return missing.toString();
    }

    private void addMissing(List<String> missing, String itemId) {
        if (!inventory.contains(itemId)) {
            missing.add(itemId);
        }
    }

    public static final class PlayerState {
        private final int x;
        private final int y;
        private final Direction direction;
        private final int steps;

        private PlayerState(int x, int y, Direction direction, int steps) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.steps = steps;
        }

        public static PlayerState origin() {
            return new PlayerState(0, 0, Direction.SOUTH, 0);
        }

        public static PlayerState at(Position position) {
            return new PlayerState(position.getX(), position.getY(), Direction.SOUTH, 0);
        }

        public static PlayerState of(int x, int y, Direction direction, int steps) {
            return new PlayerState(x, y, direction, steps);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getSteps() {
            return steps;
        }

        public Position getPosition() {
            return new Position(x, y);
        }

        private PlayerState withDirection(Direction nextDirection) {
            return new PlayerState(x, y, nextDirection, steps);
        }

        private PlayerState moveTo(Position position) {
            return new PlayerState(position.getX(), position.getY(), direction, steps + 1);
        }
    }

    private static boolean[][] createVisibleFor(World world, PlayerState player) {
        boolean[][] nextVisible = new boolean[world.getHeight()][world.getWidth()];
        markVision(world, player, nextVisible);
        return nextVisible;
    }

    private static boolean[][] createExploredFor(World world, PlayerState player) {
        boolean[][] nextExplored = new boolean[world.getHeight()][world.getWidth()];
        markVision(world, player, nextExplored);
        return nextExplored;
    }

    private static boolean[][] createExploredFor(World world, PlayerState player, boolean[][] previousExplored) {
        boolean[][] nextExplored = copyGrid(previousExplored);
        if (nextExplored == null) {
            nextExplored = new boolean[world.getHeight()][world.getWidth()];
        }
        markVision(world, player, nextExplored);
        return nextExplored;
    }

    private static void markVision(World world, PlayerState player, boolean[][] target) {
        for (int y = player.getY() - VISION_RADIUS; y <= player.getY() + VISION_RADIUS; y++) {
            for (int x = player.getX() - VISION_RADIUS; x <= player.getX() + VISION_RADIUS; x++) {
                if (world.contains(x, y)) {
                    target[y][x] = true;
                }
            }
        }
    }

    private static boolean isMarked(boolean[][] grid, int x, int y) {
        return grid != null && y >= 0 && y < grid.length && x >= 0 && x < grid[y].length && grid[y][x];
    }

    private static int countMarked(boolean[][] grid) {
        if (grid == null) {
            return 0;
        }
        int count = 0;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x]) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean[][] copyGrid(boolean[][] source) {
        if (source == null) {
            return null;
        }
        boolean[][] copy = new boolean[source.length][];
        for (int y = 0; y < source.length; y++) {
            copy[y] = new boolean[source[y].length];
            System.arraycopy(source[y], 0, copy[y], 0, source[y].length);
        }
        return copy;
    }

    private static List<Item> createItems(World world) {
        List<String> ids = new ArrayList<String>();
        ids.add(STUDENT_CARD);
        ids.add(USB);
        ids.add(REPORT);
        ids.add(LAPTOP);
        ids.add(SLIDES);
        ids.add(PASS);

        List<Item> result = new ArrayList<Item>();
        List<Room> rooms = world.getRooms();
        List<Room> candidateRooms = itemRooms(world);
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            result.add(new Item(id, displayName(id), candidateRooms.get(i % candidateRooms.size()).getCenter(), false));
        }
        return result;
    }

    private static List<Room> itemRooms(World world) {
        List<Room> result = new ArrayList<Room>();
        for (Room room : world.getRooms()) {
            Position center = room.getCenter();
            if (!center.equals(world.getSpawnPosition()) && !center.equals(world.getDefenseHallPosition())) {
                result.add(room);
            }
        }
        if (result.isEmpty()) {
            result.addAll(world.getRooms());
        }
        return result;
    }

    private static String displayName(String itemId) {
        if (STUDENT_CARD.equals(itemId)) {
            return "student card";
        }
        return itemId;
    }
}
