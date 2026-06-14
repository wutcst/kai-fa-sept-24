package cn.edu.whut.sept.zuul;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Stores the player's location, movement history, inventory and carry limit.
 */
public class Player
{
    private final String name;
    private Room currentRoom;
    private int maxCarryWeight;
    private final Deque<Room> roomHistory;
    private final List<Item> inventory;

    public Player(String name, Room startingRoom, int maxCarryWeight)
    {
        if(startingRoom == null) {
            throw new IllegalArgumentException("Starting room is required.");
        }
        if(maxCarryWeight < 0) {
            throw new IllegalArgumentException("Carry limit must not be negative.");
        }

        this.name = name;
        this.currentRoom = startingRoom;
        this.maxCarryWeight = maxCarryWeight;
        this.roomHistory = new ArrayDeque<Room>();
        this.inventory = new ArrayList<Item>();
    }

    public String getName()
    {
        return name;
    }

    public Room getCurrentRoom()
    {
        return currentRoom;
    }

    public void moveTo(Room room)
    {
        roomHistory.push(currentRoom);
        currentRoom = room;
    }

    public void teleportTo(Room room)
    {
        currentRoom = room;
    }

    public boolean goBack()
    {
        if(roomHistory.isEmpty()) {
            return false;
        }

        currentRoom = roomHistory.pop();
        return true;
    }

    public int getMaxCarryWeight()
    {
        return maxCarryWeight;
    }

    public void increaseCarryCapacity(int bonus)
    {
        maxCarryWeight += bonus;
    }

    public int getCurrentCarryWeight()
    {
        int total = 0;
        for(Item item : inventory) {
            total += item.getWeight();
        }
        return total;
    }

    public boolean canCarry(Item item)
    {
        return getCurrentCarryWeight() + item.getWeight() <= maxCarryWeight;
    }

    public boolean take(Item item)
    {
        if(item == null || !canCarry(item)) {
            return false;
        }

        inventory.add(item);
        return true;
    }

    public Item removeItem(String itemName)
    {
        if(itemName == null) {
            return null;
        }

        for(Iterator<Item> iterator = inventory.iterator(); iterator.hasNext(); ) {
            Item item = iterator.next();
            if(item.getName().equals(itemName.toLowerCase())) {
                iterator.remove();
                return item;
            }
        }
        return null;
    }

    public boolean hasItem(String itemName)
    {
        if(itemName == null) {
            return false;
        }

        for(Item item : inventory) {
            if(item.getName().equals(itemName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public List<Item> getInventory()
    {
        return Collections.unmodifiableList(inventory);
    }

    public String getInventoryDescription()
    {
        StringBuilder builder = new StringBuilder("Inventory:");
        if(inventory.isEmpty()) {
            builder.append(" none");
        } else {
            for(Item item : inventory) {
                builder.append("\n - ").append(item.getDisplayText());
            }
        }
        builder.append("\nCarry weight: ")
                .append(getCurrentCarryWeight())
                .append("/")
                .append(maxCarryWeight)
                .append("kg");
        return builder.toString();
    }
}
