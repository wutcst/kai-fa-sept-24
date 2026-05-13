package cn.edu.whut.sept.zuul;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 表示游戏中的一个房间。
 */
public class Room {
    private final String description;
    private final Map<String, Room> exits;
    private final Map<String, Item> items;
    private boolean transportRoom;

    /**
     * 创建房间。
     *
     * @param description 房间描述
     */
    public Room(String description) {
        this.description = description;
        exits = new HashMap<>();
        items = new LinkedHashMap<>();
    }

    /**
     * 设置指定方向的出口。
     *
     * @param direction 出口方向
     * @param neighbor 相邻房间
     */
    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    /**
     * 获取简短房间描述。
     *
     * @return 房间描述
     */
    public String getShortDescription() {
        return description;
    }

    /**
     * 获取房间完整描述。
     *
     * @return 当前房间、出口和物品信息
     */
    public String getLongDescription() {
        return "You are " + description + ".\n" + getExitString() + "\n" + getItemsDescription();
    }

    /**
     * 获取出口文字。
     *
     * @return 当前房间全部出口
     */
    public String getExitString() {
        StringBuilder builder = new StringBuilder("Exits:");
        Set<String> keys = exits.keySet();
        for (String exit : keys) {
            builder.append(" ").append(exit);
        }
        return builder.toString();
    }

    /**
     * 获取指定方向的相邻房间。
     *
     * @param direction 出口方向
     * @return 相邻房间，不存在时返回 null
     */
    public Room getExit(String direction) {
        return exits.get(direction);
    }

    /**
     * 增加房间物品。
     *
     * @param item 待加入物品
     */
    public void addItem(Item item) {
        items.put(item.getName(), item);
    }

    /**
     * 移除房间中的指定物品。
     *
     * @param name 物品名称
     * @return 被移除的物品，不存在时返回 null
     */
    public Item removeItem(String name) {
        return items.remove(name);
    }

    /**
     * 查找房间中的指定物品。
     *
     * @param name 物品名称
     * @return 物品，不存在时返回 null
     */
    public Item getItem(String name) {
        return items.get(name);
    }

    /**
     * 获取房间中的全部物品。
     *
     * @return 房间物品集合
     */
    public Collection<Item> getItems() {
        return items.values();
    }

    /**
     * 获取房间物品总重量。
     *
     * @return 物品总重量
     */
    public int getItemsWeight() {
        int totalWeight = 0;
        for (Item item : items.values()) {
            totalWeight += item.getWeight();
        }
        return totalWeight;
    }

    /**
     * 获取房间物品描述。
     *
     * @return 适合终端展示的物品清单
     */
    public String getItemsDescription() {
        if (items.isEmpty()) {
            return "Items here: no items. Total weight: 0";
        }

        StringBuilder builder = new StringBuilder("Items here:");
        for (Item item : items.values()) {
            builder.append("\n  ").append(item.getLongDescription());
        }
        builder.append("\nTotal room item weight: ").append(getItemsWeight());
        return builder.toString();
    }

    /**
     * 标记当前房间是否为传送房间。
     *
     * @param transportRoom true 表示进入后会触发传送
     */
    public void setTransportRoom(boolean transportRoom) {
        this.transportRoom = transportRoom;
    }

    /**
     * 判断当前房间是否为传送房间。
     *
     * @return true 表示传送房间
     */
    public boolean isTransportRoom() {
        return transportRoom;
    }
}
