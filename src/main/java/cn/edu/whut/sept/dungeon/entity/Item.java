package cn.edu.whut.sept.dungeon.entity;

import cn.edu.whut.sept.dungeon.world.Position;

public final class Item {
    private final String id;
    private final String name;
    private final Position position;
    private final boolean collected;

    public Item(String id, String name, Position position, boolean collected) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.collected = collected;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isCollected() {
        return collected;
    }

    public Item collect() {
        return new Item(id, name, position, true);
    }
}
