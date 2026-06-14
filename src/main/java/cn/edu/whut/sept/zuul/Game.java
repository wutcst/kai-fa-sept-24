/**
 * 该类是“World-of-Zuul”应用程序的主类。
 * 《World of Zuul》是一款简单的文本冒险游戏。用户可以在一些房间组成的迷宫中探险。
 * 你们可以通过扩展该游戏的功能使它更有趣!.
 *
 * 如果想开始执行这个游戏，用户需要创建Game类的一个实例并调用“play”方法。
 *
 * Game类的实例将创建并初始化所有其他类:它创建所有房间，并将它们连接成迷宫；它创建解析器
 * 接收用户输入，并将用户输入转换成命令后开始运行游戏。
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class Game
{
    private Parser parser;
    private Player player;
    private boolean finished;

    public Game()
    {
        createRooms();
        parser = new Parser();
        finished = false;
    }

    private void createRooms()
    {
        Room outside, theater, pub, lab, office;

        // create the rooms
        outside = new Room("outside the main entrance of the university");
        theater = new Room("in a lecture theater");
        pub = new Room("in the campus pub");
        lab = new Room("in a computing lab");
        office = new Room("in the computing admin office");

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        libraryItems(outside, theater, pub, lab, office);

        player = new Player("adventurer", outside, 8);
    }

    private void libraryItems(Room outside, Room theater, Room pub, Room lab, Room office)
    {
        outside.addItem(new Item("map", "a campus map with hand-written notes", 1));
        theater.addItem(new Item("report", "the printed practice report draft", 2));
        pub.addItem(new Item("coin", "a lucky coin for the final presentation", 1));
        lab.addItem(new Item("laptop", "a laptop prepared for the project demo", 4));
        office.addItem(new Item("cookie", "a magic cookie that improves your carrying capacity", 0, 5));
    }

    public void play()
    {
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.

        while (! finished) {
            Command command = parser.getCommand();
            if(command == null) {
                System.out.println("I don't understand...");
            } else {
                boolean shouldQuit = command.execute(this);
                finished = finished || shouldQuit;
            }
        }

        System.out.println("Thank you for playing.  Good bye.");
    }

    private void printWelcome()
    {
        System.out.println();
        System.out.println("Welcome to the World of Zuul!");
        System.out.println("World of Zuul is a new, incredibly boring adventure game.");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        System.out.println(player.getCurrentRoom().getLongDescription());
    }

    public Room getCurrentRoom() {
        return player.getCurrentRoom();
    }

    public void setCurrentRoom(Room room){
        player.teleportTo(room);
    }

    public Player getPlayer()
    {
        return player;
    }

    public boolean goRoom(String direction)
    {
        Room nextRoom = player.getCurrentRoom().getExit(direction);
        if(nextRoom == null) {
            return false;
        }

        player.moveTo(nextRoom);
        return true;
    }

    public boolean goBack()
    {
        return player.goBack();
    }

    public TakeResult takeItem(String itemName)
    {
        Room room = player.getCurrentRoom();
        Item item = room.findItem(itemName);
        if(item == null) {
            return TakeResult.notFound();
        }

        if(!player.canCarry(item)) {
            return TakeResult.tooHeavy(item);
        }

        Item removed = room.removeItem(itemName);
        player.take(removed);
        return TakeResult.taken(removed);
    }

    public Item dropItem(String itemName)
    {
        Item item = player.removeItem(itemName);
        if(item != null) {
            player.getCurrentRoom().addItem(item);
        }
        return item;
    }

    public Item eatCookie()
    {
        Item cookie = player.removeItem("cookie");
        if(cookie == null || !cookie.isMagicCookie()) {
            if(cookie != null) {
                player.take(cookie);
            }
            return null;
        }

        player.increaseCarryCapacity(cookie.getCapacityBonus());
        return cookie;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public static class TakeResult
    {
        private final Item item;
        private final boolean taken;
        private final boolean tooHeavy;

        private TakeResult(Item item, boolean taken, boolean tooHeavy)
        {
            this.item = item;
            this.taken = taken;
            this.tooHeavy = tooHeavy;
        }

        public static TakeResult taken(Item item)
        {
            return new TakeResult(item, true, false);
        }

        public static TakeResult tooHeavy(Item item)
        {
            return new TakeResult(item, false, true);
        }

        public static TakeResult notFound()
        {
            return new TakeResult(null, false, false);
        }

        public Item getItem()
        {
            return item;
        }

        public boolean isTaken()
        {
            return taken;
        }

        public boolean isTooHeavy()
        {
            return tooHeavy;
        }
    }
}
