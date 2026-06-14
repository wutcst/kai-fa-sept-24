package cn.edu.whut.sept.zuul;

/**
 * Represents an item that can appear in a room or be carried by the player.
 */
public class Item
{
    private final String name;
    private final String description;
    private final int weight;
    private final int capacityBonus;

    public Item(String name, String description, int weight)
    {
        this(name, description, weight, 0);
    }

    public Item(String name, String description, int weight, int capacityBonus)
    {
        if(name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name must not be empty.");
        }
        if(weight < 0) {
            throw new IllegalArgumentException("Item weight must not be negative.");
        }
        if(capacityBonus < 0) {
            throw new IllegalArgumentException("Capacity bonus must not be negative.");
        }

        this.name = name.toLowerCase();
        this.description = description;
        this.weight = weight;
        this.capacityBonus = capacityBonus;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getWeight()
    {
        return weight;
    }

    public int getCapacityBonus()
    {
        return capacityBonus;
    }

    public boolean isMagicCookie()
    {
        return capacityBonus > 0;
    }

    public String getDisplayText()
    {
        return name + " - " + description + " (" + weight + "kg)";
    }
}
