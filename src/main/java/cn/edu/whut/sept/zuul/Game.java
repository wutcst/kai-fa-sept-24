package cn.edu.whut.sept.zuul;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * World-of-Zuul 文本冒险游戏主类。
 */
public class Game {
    private static final int INITIAL_MAX_CARRY_WEIGHT = 5;
    private static final int COOKIE_WEIGHT_BONUS = 5;

    private Parser parser;
    private Player player;
    private Room portal;
    private final Deque<Room> roomHistory;
    private final List<Room> rooms;
    private final Map<String, CommandHandler> commandHandlers;
    private final Random random;

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
        roomHistory = new ArrayDeque<>();
        rooms = new ArrayList<>();
        commandHandlers = new LinkedHashMap<>();
        createRooms();
        registerCommands();
        parser = new Parser(commandHandlers.keySet());
    }

    /**
     * 创建房间、出口和初始物品。
     */
    private void createRooms() {
        Room outside = new Room("outside the main entrance of the university");
        Room theater = new Room("in a lecture theater");
        Room pub = new Room("in the campus pub");
        Room lab = new Room("in a computing lab");
        Room office = new Room("in the computing admin office");
        portal = new Room("in a strange portal room");
        portal.setTransportRoom(true);

        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);
        outside.setExit("north", portal);

        theater.setExit("west", outside);
        pub.setExit("east", outside);
        lab.setExit("north", outside);
        lab.setExit("east", office);
        office.setExit("west", lab);
        portal.setExit("south", outside);

        outside.addItem(new Item("stone", "a stone too heavy for a new player", 8));
        theater.addItem(new Item("book", "a software engineering textbook", 2));
        pub.addItem(new Item("coin", "a small coin left on the table", 1));
        lab.addItem(new Item("laptop", "a portable computer for experiments", 4));
        office.addItem(new Item("cookie", "a magic cookie that improves carrying ability", 1));

        rooms.add(outside);
        rooms.add(theater);
        rooms.add(pub);
        rooms.add(lab);
        rooms.add(office);
        rooms.add(portal);

        player = new Player("adventurer", outside, INITIAL_MAX_CARRY_WEIGHT);
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
        commandHandlers.put("quit", this::quit);
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
        System.out.println("Thank you for playing. Good bye.");
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

        roomHistory.push(currentRoom);
        enterRoom(nextRoom);
        return false;
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
        System.out.println(player.getCurrentRoom().getLongDescription());
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
        System.out.println(previousRoom.getLongDescription());
        return false;
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
        System.out.println("You take the " + itemName + ".");
        return false;
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
        System.out.println("You eat the magic cookie. Your carrying capacity is now "
                + player.getMaxCarryWeight() + ".");
        return false;
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
