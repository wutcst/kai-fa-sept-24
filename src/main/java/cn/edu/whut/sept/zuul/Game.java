package cn.edu.whut.sept.zuul;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * World-of-Zuul 文本冒险游戏主类。
 */
public class Game {
    private static final int INITIAL_MAX_CARRY_WEIGHT = 5;
    private static final int COOKIE_WEIGHT_BONUS = 5;
    private static final String SAVE_FILE = "zuul-save.properties";

    private Parser parser;
    private Player player;
    private Room outside;
    private Room office;
    private Room portal;
    private final Map<String, Room> roomsById;
    private final Map<String, Item> itemCatalog;
    private final Deque<Room> roomHistory;
    private final List<Room> rooms;
    private final Set<String> visitedRoomIds;
    private final Map<String, CommandHandler> commandHandlers;
    private final Map<String, String> aliases;
    private final Random random;
    private boolean finishedByGame;

    /**
     * 创建游戏，使用默认随机数。
     */
    public Game() {
        this(new Random());
    }

    /**
     * 创建游戏，允许测试传入固定随机数。
     *
     * @param random 传送房间使用的随机数
     */
    Game(Random random) {
        this.random = random;
        roomsById = new LinkedHashMap<>();
        itemCatalog = new LinkedHashMap<>();
        roomHistory = new ArrayDeque<>();
        rooms = new ArrayList<>();
        visitedRoomIds = new HashSet<>();
        commandHandlers = new LinkedHashMap<>();
        aliases = new LinkedHashMap<>();
        createRooms();
        registerCommands();
        registerAliases();
        parser = new Parser(getParserWords());
    }

    /**
     * 获取解析器可以识别的命令词。
     *
     * @return 命令词和别名集合
     */
    private Set<String> getParserWords() {
        Set<String> words = new LinkedHashSet<>(commandHandlers.keySet());
        words.addAll(aliases.keySet());
        return words;
    }

    /**
     * 创建房间、出口和初始物品。
     */
    private void createRooms() {
        outside = new Room("outside", "outside the main entrance of the university");
        Room theater = new Room("theater", "in a lecture theater");
        Room pub = new Room("pub", "in the campus pub");
        Room lab = new Room("lab", "in a computing lab");
        office = new Room("office", "in the computing admin office");
        Room archive = new Room("archive", "in the project archive");
        portal = new Room("portal", "in a strange portal room");
        portal.setTransportRoom(true);
        lab.setDangerous(true, 2);
        archive.setLocked(true, "key");

        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);
        outside.setExit("north", portal);

        theater.setExit("west", outside);
        pub.setExit("east", outside);
        lab.setExit("north", outside);
        lab.setExit("east", office);
        office.setExit("west", lab);
        office.setExit("east", archive);
        archive.setExit("west", office);
        portal.setExit("south", outside);

        addRoom(outside);
        addRoom(theater);
        addRoom(pub);
        addRoom(lab);
        addRoom(office);
        addRoom(archive);
        addRoom(portal);

        addItemToRoom(outside, new Item("stone", "a stone too heavy for a new player", 8));
        addItemToRoom(theater, new Item("book", "a software engineering textbook", 2));
        addItemToRoom(pub, new Item("coin", "a small coin left on the table", 1));
        addItemToRoom(pub, new Item("key", "a brass key for the project archive", 1));
        addItemToRoom(lab, new Item("laptop", "a portable computer for experiments", 4));
        addItemToRoom(office, new Item("cookie", "a magic cookie that improves carrying ability", 1));
        addItemToRoom(archive, new Item("treasure", "the final project treasure", 3));

        player = new Player("adventurer", outside, INITIAL_MAX_CARRY_WEIGHT);
        visitedRoomIds.add(outside.getId());
    }

    /**
     * 加入房间索引。
     *
     * @param room 待加入房间
     */
    private void addRoom(Room room) {
        rooms.add(room);
        roomsById.put(room.getId(), room);
    }

    /**
     * 放置物品，并加入物品索引。
     *
     * @param room 房间
     * @param item 物品
     */
    private void addItemToRoom(Room room, Item item) {
        itemCatalog.put(item.getName(), item);
        room.addItem(item);
    }

    /**
     * 注册命令处理器。
     */
    private void registerCommands() {
        commandHandlers.put("help", this::help);
        commandHandlers.put("go", this::goRoom);
        commandHandlers.put("look", this::look);
        commandHandlers.put("back", this::back);
        commandHandlers.put("take", this::take);
        commandHandlers.put("drop", this::drop);
        commandHandlers.put("items", this::items);
        commandHandlers.put("eat", this::eat);
        commandHandlers.put("quest", this::quest);
        commandHandlers.put("score", this::score);
        commandHandlers.put("status", this::status);
        commandHandlers.put("map", this::map);
        commandHandlers.put("save", this::save);
        commandHandlers.put("load", this::load);
        commandHandlers.put("quit", this::quit);
    }

    /**
     * 注册命令别名。
     */
    private void registerAliases() {
        aliases.put("n", "north");
        aliases.put("north", "north");
        aliases.put("s", "south");
        aliases.put("south", "south");
        aliases.put("e", "east");
        aliases.put("east", "east");
        aliases.put("w", "west");
        aliases.put("west", "west");
        aliases.put("get", "take");
    }

    /**
     * 启动游戏主循环。
     */
    public void play() {
        printWelcome();

        boolean finished = false;
        while (!finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        if (!finishedByGame) {
            System.out.println("Thank you for playing. Good bye.");
            printScore();
        }
    }

    /**
     * 输出欢迎信息。
     */
    private void printWelcome() {
        System.out.println();
        System.out.println("Welcome to the World of Zuul!");
        System.out.println("World of Zuul is a text adventure game.");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        System.out.println(player.getCurrentRoom().getLongDescription());
    }

    /**
     * 执行玩家命令。
     *
     * @param command 待执行命令
     * @return 是否结束游戏
     */
    boolean processCommand(Command command) {
        command = normalizeCommand(command);
        if (command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        CommandHandler handler = commandHandlers.get(command.getCommandWord());
        if (handler == null) {
            System.out.println("I don't know what you mean...");
            return false;
        }
        return handler.execute(command);
    }

    /**
     * 处理方向简写和命令别名。
     *
     * @param command 原始命令
     * @return 规范化后的命令
     */
    private Command normalizeCommand(Command command) {
        if (command.isUnknown()) {
            return command;
        }

        String commandWord = command.getCommandWord();
        String secondWord = command.getSecondWord();
        if (aliases.containsKey(commandWord)) {
            String aliasValue = aliases.get(commandWord);
            if (isDirection(aliasValue)) {
                return new Command("go", aliasValue);
            }
            return new Command(aliasValue, secondWord);
        }
        return command;
    }

    /**
     * 判断文本是否是方向。
     *
     * @param value 待检查文本
     * @return 是方向时返回 true
     */
    private boolean isDirection(String value) {
        return "north".equals(value) || "south".equals(value)
                || "east".equals(value) || "west".equals(value);
    }

    /**
     * 处理 help 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean help(Command command) {
        printHelp();
        return false;
    }

    /**
     * 输出帮助信息。
     */
    private void printHelp() {
        System.out.println("You are lost. You are alone. You wander");
        System.out.println("around at the university.");
        System.out.println();
        System.out.println("Your command words are:");
        parser.showCommands();
        System.out.println("Direction aliases: n, s, e, w, north, south, east, west");
        System.out.println("Command alias: get = take");
    }

    /**
     * 处理 go 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean goRoom(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Go where?");
            return false;
        }

        Room currentRoom = player.getCurrentRoom();
        Room nextRoom = currentRoom.getExit(command.getSecondWord());

        if (nextRoom == null) {
            System.out.println("There is no door!");
            return false;
        }
        if (nextRoom.isLocked() && !player.hasItem(nextRoom.getRequiredItemName())) {
            System.out.println("The door is locked. You need " + nextRoom.getRequiredItemName() + ".");
            return false;
        }
        if (nextRoom.isLocked()) {
            String requiredItemName = nextRoom.getRequiredItemName();
            nextRoom.setLocked(false, null);
            player.addScore(10);
            System.out.println("You unlock the door with the " + requiredItemName + ".");
        }

        roomHistory.push(currentRoom);
        enterRoom(nextRoom);
        return checkGameResult();
    }

    /**
     * 进入房间，并在需要时触发传送。
     *
     * @param nextRoom 即将进入的房间
     */
    private void enterRoom(Room nextRoom) {
        player.setCurrentRoom(nextRoom);
        if (nextRoom.isTransportRoom()) {
            Room destination = chooseTransportDestination(nextRoom);
            System.out.println("The portal flashes. You are transported away!");
            player.setCurrentRoom(destination);
        }
        visitedRoomIds.add(player.getCurrentRoom().getId());
        applyRoomEffect(player.getCurrentRoom());
        System.out.println(player.getCurrentRoom().getLongDescription());
    }

    /**
     * 应用房间效果。
     *
     * @param room 当前房间
     */
    private void applyRoomEffect(Room room) {
        if (room.isDangerous()) {
            player.hurt(room.getDamage());
            System.out.println("The room is dangerous. You lose " + room.getDamage()
                    + " health. Health: " + player.getHealth());
        }
    }

    /**
     * 选择传送目的地。
     *
     * @param source 传送起点
     * @return 传送目的地
     */
    private Room chooseTransportDestination(Room source) {
        List<Room> destinations = new ArrayList<>(rooms);
        destinations.remove(source);
        return destinations.get(random.nextInt(destinations.size()));
    }

    /**
     * 处理 look 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean look(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Look what?");
            return false;
        }
        System.out.println(player.getCurrentRoom().getLongDescription());
        return false;
    }

    /**
     * 处理 back 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean back(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Back what?");
            return false;
        }
        if (roomHistory.isEmpty()) {
            System.out.println("You are at the starting point.");
            return false;
        }

        Room previousRoom = roomHistory.pop();
        player.setCurrentRoom(previousRoom);
        visitedRoomIds.add(previousRoom.getId());
        System.out.println(previousRoom.getLongDescription());
        return checkGameResult();
    }

    /**
     * 处理 take 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean take(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Take what?");
            return false;
        }

        String itemName = command.getSecondWord();
        Room currentRoom = player.getCurrentRoom();
        Item item = currentRoom.getItem(itemName);
        if (item == null) {
            System.out.println("There is no " + itemName + " here.");
            return false;
        }
        if (!player.canCarry(item)) {
            System.out.println("The " + itemName + " is too heavy for you.");
            return false;
        }

        currentRoom.removeItem(itemName);
        player.addItem(item);
        player.addScore(item.getWeight());
        System.out.println("You take the " + itemName + ".");
        return checkGameResult();
    }

    /**
     * 处理 drop 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean drop(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Drop what?");
            return false;
        }

        String itemName = command.getSecondWord();
        if ("all".equals(itemName)) {
            dropAllItems();
            return false;
        }

        Item item = player.removeItem(itemName);
        if (item == null) {
            System.out.println("You do not have " + itemName + ".");
            return false;
        }

        player.getCurrentRoom().addItem(item);
        System.out.println("You drop the " + itemName + ".");
        return false;
    }

    /**
     * 丢弃背包中全部物品。
     */
    private void dropAllItems() {
        if (player.getInventoryItems().isEmpty()) {
            System.out.println("You have no items to drop.");
            return;
        }

        Item[] itemsToDrop = player.getInventoryItems().toArray(new Item[0]);
        for (Item item : itemsToDrop) {
            player.removeItem(item.getName());
            player.getCurrentRoom().addItem(item);
        }
        System.out.println("You drop all items.");
    }

    /**
     * 处理 items 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean items(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Items what?");
            return false;
        }
        System.out.println(player.getCurrentRoom().getItemsDescription());
        System.out.println(player.getInventoryDescription());
        return false;
    }

    /**
     * 处理 eat 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean eat(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Eat what?");
            return false;
        }
        if (!"cookie".equals(command.getSecondWord())) {
            System.out.println("You cannot eat that.");
            return false;
        }

        Item cookie = player.removeItem("cookie");
        if (cookie == null) {
            cookie = player.getCurrentRoom().removeItem("cookie");
        }
        if (cookie == null) {
            System.out.println("There is no cookie to eat.");
            return false;
        }

        player.increaseMaxCarryWeight(COOKIE_WEIGHT_BONUS);
        player.addScore(5);
        System.out.println("You eat the magic cookie. Your carrying capacity is now "
                + player.getMaxCarryWeight() + ".");
        return false;
    }

    /**
     * 处理 quest 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean quest(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Quest what?");
            return false;
        }
        if (!player.hasItem("key")) {
            System.out.println("Quest: find the key in the pub.");
        }
        else if (!player.hasItem("treasure")) {
            System.out.println("Quest: unlock the archive and take the treasure.");
        }
        else if (player.getCurrentRoom() != outside) {
            System.out.println("Quest: return to the entrance with the treasure.");
        }
        else {
            System.out.println("Quest complete. Use any command to finish the adventure.");
        }
        return false;
    }

    /**
     * 处理 score 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean score(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Score what?");
            return false;
        }
        printScore();
        return false;
    }

    /**
     * 输出分数。
     */
    private void printScore() {
        System.out.println("Score: " + player.getScore());
    }

    /**
     * 处理 status 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean status(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Status what?");
            return false;
        }
        System.out.println("Player: " + player.getName());
        System.out.println("Health: " + player.getHealth());
        System.out.println("Score: " + player.getScore());
        System.out.println(player.getInventoryDescription());
        return false;
    }

    /**
     * 处理 map 命令。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean map(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Map what?");
            return false;
        }
        for (Room room : rooms) {
            String marker = room == player.getCurrentRoom() ? "* " : "  ";
            String visited = visitedRoomIds.contains(room.getId()) ? "visited" : "unknown";
            System.out.println(marker + room.getId() + " - " + room.getShortDescription() + " (" + visited + ")");
        }
        return false;
    }

    /**
     * 保存游戏进度。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean save(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Save what?");
            return false;
        }

        Properties properties = new Properties();
        properties.setProperty("room", player.getCurrentRoom().getId());
        properties.setProperty("health", String.valueOf(player.getHealth()));
        properties.setProperty("score", String.valueOf(player.getScore()));
        properties.setProperty("maxCarryWeight", String.valueOf(player.getMaxCarryWeight()));
        properties.setProperty("inventory", joinItemNames(player.getInventoryItems()));
        properties.setProperty("visited", String.join(",", visitedRoomIds));
        for (Room room : rooms) {
            properties.setProperty("room." + room.getId() + ".items", joinItemNames(room.getItems()));
            properties.setProperty("room." + room.getId() + ".locked", String.valueOf(room.isLocked()));
        }

        try (FileOutputStream output = new FileOutputStream(SAVE_FILE)) {
            properties.store(output, "World of Zuul save");
            System.out.println("Game saved to " + SAVE_FILE + ".");
        }
        catch (IOException exception) {
            System.out.println("Could not save game: " + exception.getMessage());
        }
        return false;
    }

    /**
     * 读取游戏进度。
     *
     * @param command 用户命令
     * @return false，游戏继续
     */
    private boolean load(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Load what?");
            return false;
        }

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(SAVE_FILE)) {
            properties.load(input);
            restoreFromProperties(properties);
            System.out.println("Game loaded from " + SAVE_FILE + ".");
            System.out.println(player.getCurrentRoom().getLongDescription());
        }
        catch (IOException exception) {
            System.out.println("Could not load game: " + exception.getMessage());
        }
        return false;
    }

    /**
     * 根据存档属性恢复状态。
     *
     * @param properties 存档属性
     */
    private void restoreFromProperties(Properties properties) {
        for (Room room : rooms) {
            room.clearItems();
        }
        player.clearInventory();
        roomHistory.clear();
        visitedRoomIds.clear();

        for (Room room : rooms) {
            addItemsByName(room, properties.getProperty("room." + room.getId() + ".items", ""));
            room.setLocked(Boolean.parseBoolean(properties.getProperty("room." + room.getId() + ".locked", "false")),
                    room.getRequiredItemName());
        }
        addItemsByName(player, properties.getProperty("inventory", ""));

        Room savedRoom = roomsById.get(properties.getProperty("room", outside.getId()));
        player.setCurrentRoom(savedRoom == null ? outside : savedRoom);
        player.setHealth(Integer.parseInt(properties.getProperty("health", "10")));
        player.setScore(Integer.parseInt(properties.getProperty("score", "0")));
        player.setMaxCarryWeight(Integer.parseInt(properties.getProperty("maxCarryWeight",
                String.valueOf(INITIAL_MAX_CARRY_WEIGHT))));

        String visited = properties.getProperty("visited", outside.getId());
        for (String roomId : visited.split(",")) {
            if (!roomId.isEmpty()) {
                visitedRoomIds.add(roomId);
            }
        }
        visitedRoomIds.add(player.getCurrentRoom().getId());
    }

    /**
     * 把物品名称列表加入房间。
     *
     * @param room 房间
     * @param itemNames 物品名称列表
     */
    private void addItemsByName(Room room, String itemNames) {
        for (String itemName : itemNames.split(",")) {
            Item item = itemCatalog.get(itemName);
            if (item != null) {
                room.addItem(item);
            }
        }
    }

    /**
     * 把物品名称列表加入玩家背包。
     *
     * @param targetPlayer 玩家
     * @param itemNames 物品名称列表
     */
    private void addItemsByName(Player targetPlayer, String itemNames) {
        for (String itemName : itemNames.split(",")) {
            Item item = itemCatalog.get(itemName);
            if (item != null) {
                targetPlayer.addItem(item);
            }
        }
    }

    /**
     * 拼接物品名称。
     *
     * @param items 物品集合
     * @return 逗号分隔的物品名称
     */
    private String joinItemNames(Collection<Item> items) {
        List<String> names = new ArrayList<>();
        for (Item item : items) {
            names.add(item.getName());
        }
        return String.join(",", names);
    }

    /**
     * 处理 quit 命令。
     *
     * @param command 用户命令
     * @return true 表示退出游戏
     */
    private boolean quit(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        }
        return true;
    }

    /**
     * 检查游戏是否胜利或失败。
     *
     * @return true 表示游戏结束
     */
    private boolean checkGameResult() {
        if (player.getHealth() <= 0) {
            finishedByGame = true;
            System.out.println("You collapse from exhaustion. Game over.");
            printScore();
            return true;
        }
        if (player.hasItem("treasure") && player.getCurrentRoom() == outside) {
            player.addScore(50);
            finishedByGame = true;
            System.out.println("You return to the entrance with the treasure. You win!");
            printScore();
            return true;
        }
        return false;
    }

    /**
     * 获取玩家对象，供测试验证。
     *
     * @return 当前玩家
     */
    Player getPlayer() {
        return player;
    }

    /**
     * 获取传送房间，供测试验证。
     *
     * @return 传送房间
     */
    Room getPortal() {
        return portal;
    }

    /**
     * 游戏命令处理函数。
     */
    @FunctionalInterface
    private interface CommandHandler {
        /**
         * 执行命令。
         *
         * @param command 待执行命令
         * @return 是否结束游戏
         */
        boolean execute(Command command);
    }
}
