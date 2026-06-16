package cn.edu.whut.sept.dungeon.render;

import cn.edu.whut.sept.dungeon.core.GameState;
import cn.edu.whut.sept.dungeon.core.VisibilityState;
import cn.edu.whut.sept.dungeon.entity.Enemy;
import cn.edu.whut.sept.dungeon.entity.Item;
import cn.edu.whut.sept.dungeon.entity.Npc;
import cn.edu.whut.sept.dungeon.entity.Trap;
import cn.edu.whut.sept.dungeon.projectile.Projectile;
import cn.edu.whut.sept.dungeon.room.RoomState;
import cn.edu.whut.sept.dungeon.room.RoomStatus;
import cn.edu.whut.sept.dungeon.world.Room;
import cn.edu.whut.sept.dungeon.world.Position;
import cn.edu.whut.sept.dungeon.world.Tile;
import cn.edu.whut.sept.dungeon.world.World;

import java.awt.Color;
import java.awt.Graphics2D;

public final class TileRenderer {
    public static final int TILE_SIZE = 24;
    public static final int VIEWPORT_WIDTH = 40;
    public static final int VIEWPORT_HEIGHT = 24;
    public static final Color UNEXPLORED_COLOR = Color.BLACK;
    public static final Color SEEN_WALL_COLOR = new Color(34, 38, 45);
    public static final Color SEEN_FLOOR_COLOR = new Color(45, 48, 53);
    public static final Color WALL_COLOR = new Color(71, 78, 90);
    public static final Color FLOOR_COLOR = new Color(184, 178, 158);
    public static final Color PLAYER_COLOR = new Color(70, 130, 230);
    public static final Color DEFENSE_HALL_COLOR = new Color(208, 180, 75);
    public static final Color ITEM_COLOR = new Color(96, 170, 105);
    public static final Color NPC_COLOR = new Color(178, 105, 210);
    public static final Color ENEMY_COLOR = new Color(200, 70, 70);
    public static final Color PROJECTILE_COLOR = new Color(80, 220, 230);
    public static final Color STAIRS_COLOR = new Color(110, 210, 220);
    public static final Color TRAP_COLOR = new Color(230, 120, 45);
    public static final Color ROOM_LOCK_COLOR = new Color(238, 115, 55);

    public Color colorFor(GameState state, int x, int y) {
        if (state == null || state.getWorld() == null) {
            return UNEXPLORED_COLOR;
        }
        World world = state.getWorld();
        Tile tile = world.getTile(x, y);
        VisibilityState visibilityState = state.getVisibilityState(x, y);
        switch (visibilityState) {
            case UNSEEN:
                return UNEXPLORED_COLOR;
            case SEEN:
                return tile == Tile.WALL ? SEEN_WALL_COLOR : SEEN_FLOOR_COLOR;
            case VISIBLE:
                if (isPlayerAt(state, x, y)) {
                    return PLAYER_COLOR;
                }
                if (isEnemyAt(state, x, y)) {
                    return ENEMY_COLOR;
                }
                if (isProjectileAt(state, x, y)) {
                    return PROJECTILE_COLOR;
                }
                if (isItemAt(state, x, y)) {
                    return ITEM_COLOR;
                }
                if (isNpcAt(state, x, y)) {
                    return NPC_COLOR;
                }
                if (isTrapAt(state, x, y)) {
                    return TRAP_COLOR;
                }
                if (isStairsAt(state, x, y)) {
                    return STAIRS_COLOR;
                }
                if (isDefenseHallAt(world, x, y)) {
                    return DEFENSE_HALL_COLOR;
                }
                return tile == Tile.WALL ? WALL_COLOR : FLOOR_COLOR;
            default:
                return UNEXPLORED_COLOR;
        }
    }

    public String glyphFor(GameState state, int x, int y) {
        if (state == null || state.getWorld() == null || state.getVisibilityState(x, y) != VisibilityState.VISIBLE) {
            return "";
        }
        if (isPlayerAt(state, x, y)) {
            switch (state.getPlayer().getDirection()) {
                case NORTH:
                    return "^";
                case SOUTH:
                    return "v";
                case WEST:
                    return "<";
                case EAST:
                    return ">";
                default:
                    return "@";
            }
        }
        Enemy enemy = enemyAt(state, x, y);
        if (enemy != null) {
            return "Defense Committee".equals(enemy.getType()) ? "B" : "!";
        }
        if (isProjectileAt(state, x, y)) {
            return "*";
        }
        Item item = itemAt(state, x, y);
        if (item != null) {
            return itemGlyph(item);
        }
        if (isNpcAt(state, x, y)) {
            return "?";
        }
        if (isTrapAt(state, x, y)) {
            return "x";
        }
        if (isStairsAt(state, x, y)) {
            return ">";
        }
        if (isDefenseHallAt(state.getWorld(), x, y)) {
            return "#";
        }
        return "";
    }

    public void draw(GameState state, Graphics2D graphics) {
        if (state == null || state.getWorld() == null) {
            return;
        }
        World world = state.getWorld();
        int originX = cameraOrigin(state.getPlayer().getX(), world.getWidth(), VIEWPORT_WIDTH);
        int originY = cameraOrigin(state.getPlayer().getY(), world.getHeight(), VIEWPORT_HEIGHT);
        for (int screenY = 0; screenY < VIEWPORT_HEIGHT; screenY++) {
            for (int screenX = 0; screenX < VIEWPORT_WIDTH; screenX++) {
                int worldX = originX + screenX;
                int worldY = originY + screenY;
                graphics.setColor(colorFor(state, worldX, worldY));
                graphics.fillRect(screenX * TILE_SIZE, screenY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                drawTileTexture(state, graphics, worldX, worldY, screenX, screenY);
                drawEntity(state, graphics, worldX, worldY, screenX, screenY);
            }
        }
    }

    private int cameraOrigin(int center, int worldSize, int viewportSize) {
        if (worldSize <= viewportSize) {
            return 0;
        }
        int origin = center - viewportSize / 2;
        int maxOrigin = worldSize - viewportSize;
        return Math.max(0, Math.min(origin, maxOrigin));
    }

    private void drawTileTexture(GameState state, Graphics2D graphics, int worldX, int worldY, int screenX, int screenY) {
        if (state.getVisibilityState(worldX, worldY) != VisibilityState.VISIBLE) {
            return;
        }
        int drawX = screenX * TILE_SIZE;
        int drawY = screenY * TILE_SIZE;
        if (state.getWorld().getTile(worldX, worldY) == Tile.WALL) {
            graphics.setColor(new Color(92, 99, 113));
            graphics.drawLine(drawX + 3, drawY + 6, drawX + 20, drawY + 6);
            graphics.drawLine(drawX + 1, drawY + 15, drawX + 18, drawY + 15);
            graphics.setColor(new Color(50, 56, 66));
            graphics.drawLine(drawX + 8, drawY + 1, drawX + 8, drawY + 8);
            graphics.drawLine(drawX + 17, drawY + 13, drawX + 17, drawY + 22);
            drawRoomLockWarning(state, graphics, worldX, worldY, drawX, drawY);
            return;
        }
        graphics.setColor(new Color(158, 150, 128));
        graphics.drawRect(drawX + 4, drawY + 4, 3, 3);
        graphics.drawRect(drawX + 15, drawY + 14, 2, 2);
        graphics.setColor(new Color(210, 203, 181));
        graphics.drawLine(drawX + 2, drawY + 22, drawX + 21, drawY + 22);
        drawRoomLockWarning(state, graphics, worldX, worldY, drawX, drawY);
    }

    private void drawRoomLockWarning(GameState state, Graphics2D graphics, int worldX, int worldY, int drawX, int drawY) {
        RoomState roomState = state.roomStateAt(new Position(worldX, worldY));
        if (roomState == null || roomState.getStatus() != RoomStatus.ACTIVE) {
            return;
        }
        Room room = state.getWorld().getRooms().get(roomState.getId());
        if (worldX == room.getX() || worldX == room.getRight() || worldY == room.getY() || worldY == room.getBottom()) {
            graphics.setColor(ROOM_LOCK_COLOR);
            graphics.drawRect(drawX + 1, drawY + 1, TILE_SIZE - 3, TILE_SIZE - 3);
        }
    }

    private void drawEntity(GameState state, Graphics2D graphics, int worldX, int worldY, int screenX, int screenY) {
        if (state.getVisibilityState(worldX, worldY) != VisibilityState.VISIBLE) {
            return;
        }
        int drawX = screenX * TILE_SIZE;
        int drawY = screenY * TILE_SIZE;
        if (isPlayerAt(state, worldX, worldY)) {
            drawPlayer(state, graphics, drawX, drawY);
            return;
        }
        Enemy enemy = enemyAt(state, worldX, worldY);
        if (enemy != null) {
            drawEnemy(enemy, graphics, drawX, drawY);
            return;
        }
        if (isProjectileAt(state, worldX, worldY)) {
            drawProjectile(graphics, drawX, drawY);
            return;
        }
        Item item = itemAt(state, worldX, worldY);
        if (item != null) {
            drawItem(item, graphics, drawX, drawY);
            return;
        }
        if (isNpcAt(state, worldX, worldY)) {
            drawNpc(graphics, drawX, drawY);
            return;
        }
        if (isTrapAt(state, worldX, worldY)) {
            drawTrap(graphics, drawX, drawY);
            return;
        }
        if (isStairsAt(state, worldX, worldY)) {
            drawStairs(graphics, drawX, drawY);
            return;
        }
        if (isDefenseHallAt(state.getWorld(), worldX, worldY)) {
            drawDefenseHall(graphics, drawX, drawY);
        }
    }

    private void drawPlayer(GameState state, Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(22, 24, 30));
        graphics.fillRect(drawX + 6, drawY + 5, 12, 15);
        graphics.setColor(PLAYER_COLOR);
        graphics.fillRect(drawX + 8, drawY + 7, 8, 10);
        graphics.setColor(new Color(152, 196, 255));
        graphics.fillRect(drawX + 9, drawY + 3, 6, 5);
        graphics.setColor(new Color(245, 245, 220));
        int aimX = drawX + 11 + state.getPlayer().getDirection().getDx() * 6;
        int aimY = drawY + 12 + state.getPlayer().getDirection().getDy() * 6;
        graphics.fillRect(aimX, aimY, 4, 4);
    }

    private void drawEnemy(Enemy enemy, Graphics2D graphics, int drawX, int drawY) {
        if ("Defense Committee".equals(enemy.getType())) {
            graphics.setColor(new Color(80, 20, 30));
            graphics.fillRect(drawX + 3, drawY + 3, 18, 18);
            graphics.setColor(new Color(230, 200, 95));
            graphics.fillRect(drawX + 6, drawY + 6, 12, 4);
            graphics.setColor(new Color(245, 245, 245));
            graphics.fillRect(drawX + 6, drawY + 14, 4, 3);
            graphics.fillRect(drawX + 14, drawY + 14, 4, 3);
            return;
        }
        graphics.setColor(new Color(45, 18, 24));
        graphics.fillRect(drawX + 5, drawY + 6, 14, 13);
        graphics.setColor(enemy.isReviewShooter() ? new Color(170, 80, 210) : ENEMY_COLOR);
        graphics.fillRect(drawX + 7, drawY + 8, 10, 8);
        if (enemy.isDeadlineRunner()) {
            graphics.setColor(new Color(250, 170, 60));
            graphics.fillRect(drawX + 3, drawY + 10, 4, 3);
            graphics.fillRect(drawX + 17, drawY + 10, 4, 3);
        } else if (enemy.isReviewShooter()) {
            graphics.setColor(PROJECTILE_COLOR);
            graphics.fillRect(drawX + 10, drawY + 16, 4, 4);
        } else {
            graphics.setColor(new Color(120, 230, 120));
            graphics.fillRect(drawX + 9, drawY + 15, 6, 3);
        }
    }

    private void drawProjectile(Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(18, 60, 70));
        graphics.fillRect(drawX + 8, drawY + 8, 8, 8);
        graphics.setColor(PROJECTILE_COLOR);
        graphics.fillRect(drawX + 10, drawY + 10, 4, 4);
        graphics.drawLine(drawX + 7, drawY + 12, drawX + 16, drawY + 12);
        graphics.drawLine(drawX + 12, drawY + 7, drawX + 12, drawY + 16);
    }

    private void drawItem(Item item, Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(20, 38, 24));
        graphics.fillRect(drawX + 6, drawY + 7, 12, 12);
        graphics.setColor(ITEM_COLOR);
        if (item.getId().contains("potion")) {
            graphics.fillRect(drawX + 10, drawY + 5, 4, 4);
            graphics.fillRect(drawX + 8, drawY + 10, 8, 8);
            return;
        }
        if (item.getId().contains("keyboard") || item.getId().contains("blade")) {
            graphics.fillRect(drawX + 6, drawY + 14, 12, 3);
            graphics.fillRect(drawX + 15, drawY + 7, 3, 10);
            return;
        }
        graphics.fillRect(drawX + 8, drawY + 8, 8, 10);
        graphics.setColor(new Color(210, 245, 210));
        graphics.drawLine(drawX + 8, drawY + 12, drawX + 15, drawY + 12);
    }

    private void drawNpc(Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(50, 28, 62));
        graphics.fillRect(drawX + 6, drawY + 6, 12, 14);
        graphics.setColor(NPC_COLOR);
        graphics.fillRect(drawX + 9, drawY + 4, 6, 6);
        graphics.fillRect(drawX + 8, drawY + 11, 8, 8);
    }

    private void drawTrap(Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(80, 36, 20));
        graphics.fillRect(drawX + 4, drawY + 16, 16, 4);
        graphics.setColor(TRAP_COLOR);
        graphics.drawLine(drawX + 6, drawY + 16, drawX + 9, drawY + 8);
        graphics.drawLine(drawX + 12, drawY + 16, drawX + 12, drawY + 7);
        graphics.drawLine(drawX + 18, drawY + 16, drawX + 15, drawY + 8);
    }

    private void drawStairs(Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(25, 62, 68));
        graphics.fillRect(drawX + 5, drawY + 15, 14, 4);
        graphics.setColor(STAIRS_COLOR);
        graphics.fillRect(drawX + 7, drawY + 11, 12, 3);
        graphics.fillRect(drawX + 9, drawY + 7, 10, 3);
    }

    private void drawDefenseHall(Graphics2D graphics, int drawX, int drawY) {
        graphics.setColor(new Color(78, 65, 22));
        graphics.fillRect(drawX + 4, drawY + 6, 16, 14);
        graphics.setColor(DEFENSE_HALL_COLOR);
        graphics.fillRect(drawX + 7, drawY + 4, 10, 4);
        graphics.fillRect(drawX + 8, drawY + 11, 8, 6);
    }

    private boolean isPlayerAt(GameState state, int x, int y) {
        return state.getPlayer().getX() == x && state.getPlayer().getY() == y;
    }

    private boolean isDefenseHallAt(World world, int x, int y) {
        Position defenseHall = world.getDefenseHallPosition();
        return defenseHall.getX() == x && defenseHall.getY() == y;
    }

    private boolean isStairsAt(GameState state, int x, int y) {
        Position stairs = state.getWorld().getStairsPosition();
        return state.getDepth() < 5 && stairs.getX() == x && stairs.getY() == y;
    }

    private boolean isItemAt(GameState state, int x, int y) {
        return itemAt(state, x, y) != null;
    }

    private Item itemAt(GameState state, int x, int y) {
        for (Item item : state.getItems()) {
            if (!item.isCollected() && item.getPosition().getX() == x && item.getPosition().getY() == y) {
                return item;
            }
        }
        return null;
    }

    private boolean isEnemyAt(GameState state, int x, int y) {
        return enemyAt(state, x, y) != null;
    }

    private boolean isProjectileAt(GameState state, int x, int y) {
        for (Projectile projectile : state.getProjectiles()) {
            if (projectile.isAlive()
                    && projectile.getPosition().getX() == x
                    && projectile.getPosition().getY() == y) {
                return true;
            }
        }
        return false;
    }

    private Enemy enemyAt(GameState state, int x, int y) {
        for (Enemy enemy : state.getEnemies()) {
            if (enemy.isAlive() && enemy.getPosition().getX() == x && enemy.getPosition().getY() == y) {
                return enemy;
            }
        }
        return null;
    }

    private boolean isNpcAt(GameState state, int x, int y) {
        for (Npc npc : state.getNpcs()) {
            if (npc.getPosition().getX() == x && npc.getPosition().getY() == y) {
                return true;
            }
        }
        return false;
    }

    private boolean isTrapAt(GameState state, int x, int y) {
        for (Trap trap : state.getTraps()) {
            if (!trap.isTriggered() && trap.getPosition().getX() == x && trap.getPosition().getY() == y) {
                return true;
            }
        }
        return false;
    }

    private String itemGlyph(Item item) {
        String id = item.getId();
        if (id.contains("potion")) {
            return "+";
        }
        if (id.contains("keyboard") || id.contains("blade")) {
            return "/";
        }
        if (id.contains("coat") || id.contains("robe") || id.contains("suit")) {
            return "]";
        }
        return "*";
    }
}
