package cn.edu.whut.sept.zuul;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表示正在游玩的玩家。
 */
public class Player {
    private final String name;
    private Room currentRoom;
    private final Map<String, Item> inventory;
    private int maxCarryWeight;

    /**
     * 创建玩家。
     *
     * @param name 玩家姓名
     * @param currentRoom 初始房间
     * @param maxCarryWeight 最大可携带重量
     */
    public Player(String name, Room currentRoom, int maxCarryWeight) {
        this.name = name;
        this.currentRoom = currentRoom;
        this.maxCarryWeight = maxCarryWeight;
        inventory = new LinkedHashMap<>();
    }

    /**
     * 获取玩家姓名。
     *
     * @return 玩家姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取当前房间。
     *
     * @return 当前房间
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 设置当前房间。
     *
     * @param currentRoom 新房间
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * 判断玩家是否还能携带指定物品。
     *
     * @param item 待携带物品
     * @return 未超过最大负重时返回 true
     */
    public boolean canCarry(Item item) {
        return getInventoryWeight() + item.getWeight() <= maxCarryWeight;
    }

    /**
     * 将物品放入背包。
     *
     * @param item 待加入物品
     */
    public void addItem(Item item) {
        inventory.put(item.getName(), item);
    }

    /**
     * 从背包移除物品。
     *
     * @param name 物品名称
     * @return 被移除物品，不存在时返回 null
     */
    public Item removeItem(String name) {
        return inventory.remove(name);
    }

    /**
     * 查找背包中的物品。
     *
     * @param name 物品名称
     * @return 背包物品，不存在时返回 null
     */
    public Item getItem(String name) {
        return inventory.get(name);
    }

    /**
     * 获取背包物品总重量。
     *
     * @return 总重量
     */
    public int getInventoryWeight() {
        int totalWeight = 0;
        for (Item item : inventory.values()) {
            totalWeight += item.getWeight();
        }
        return totalWeight;
    }

    /**
     * 获取最大可携带重量。
     *
     * @return 最大负重
     */
    public int getMaxCarryWeight() {
        return maxCarryWeight;
    }

    /**
     * 提升最大可携带重量。
     *
     * @param extraWeight 增加重量
     */
    public void increaseMaxCarryWeight(int extraWeight) {
        maxCarryWeight += extraWeight;
    }

    /**
     * 获取背包全部物品。
     *
     * @return 背包物品集合
     */
    public Collection<Item> getInventoryItems() {
        return inventory.values();
    }

    /**
     * 获取背包文字描述。
     *
     * @return 适合终端展示的背包信息
     */
    public String getInventoryDescription() {
        if (inventory.isEmpty()) {
            return "Inventory: no items. Total weight: 0/" + maxCarryWeight;
        }

        StringBuilder builder = new StringBuilder("Inventory:");
        for (Item item : inventory.values()) {
            builder.append("\n  ").append(item.getLongDescription());
        }
        builder.append("\nTotal weight: ").append(getInventoryWeight()).append("/").append(maxCarryWeight);
        return builder.toString();
    }
}
