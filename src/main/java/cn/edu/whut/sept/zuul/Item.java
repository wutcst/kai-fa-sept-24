package cn.edu.whut.sept.zuul;

/**
 * 表示游戏世界中的物品。
 */
public class Item {
    private final String name;
    private final String description;
    private final int weight;

    /**
     * 创建一个物品。
     *
     * @param name 物品名称，用于命令识别
     * @param description 物品说明，用于展示给玩家
     * @param weight 物品重量，用于负重判断
     */
    public Item(String name, String description, int weight) {
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    /**
     * 获取物品名称。
     *
     * @return 物品名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取物品描述。
     *
     * @return 物品描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取物品重量。
     *
     * @return 物品重量
     */
    public int getWeight() {
        return weight;
    }

    /**
     * 获取适合终端输出的完整物品信息。
     *
     * @return 物品名称、描述和重量
     */
    public String getLongDescription() {
        return name + " - " + description + " (weight: " + weight + ")";
    }
}
