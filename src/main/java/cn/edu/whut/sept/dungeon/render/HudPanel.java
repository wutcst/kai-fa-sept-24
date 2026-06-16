package cn.edu.whut.sept.dungeon.render;

import cn.edu.whut.sept.dungeon.core.GameState;
import cn.edu.whut.sept.dungeon.room.RoomState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;

public final class HudPanel extends JPanel {
    private static final int HUD_HEIGHT = 112;
    private static final int MAX_LINE_CHARS = 118;
    private static final int HUD_WIDTH = TileRenderer.VIEWPORT_WIDTH * TileRenderer.TILE_SIZE;
    private static final List<String> DEFENSE_MATERIALS = Arrays.asList("report", "laptop", "slides", "pass");
    private GameState state;

    public HudPanel() {
        setPreferredSize(new Dimension(HUD_WIDTH, HUD_HEIGHT));
        setBackground(new Color(18, 21, 26));
        setFocusable(false);
    }

    public void setState(GameState state) {
        this.state = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawPanelBackdrop(graphics);
        graphics.setColor(new Color(238, 241, 232));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        graphics.drawString(primaryText(), 20, 25);
        drawHealthBar(graphics, 20, 38, 230, 16);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        graphics.setColor(new Color(210, 216, 205));
        graphics.drawString(combatText(), 270, 52);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        graphics.setColor(new Color(176, 187, 196));
        graphics.drawString(goalText(), 20, 78);
        graphics.setColor(new Color(245, 206, 118));
        graphics.drawString(messageText(), 20, 101);
    }

    private void drawPanelBackdrop(Graphics graphics) {
        graphics.setColor(new Color(26, 31, 38));
        graphics.fillRect(10, 8, HUD_WIDTH - 20, HUD_HEIGHT - 16);
        graphics.setColor(new Color(54, 61, 72));
        graphics.drawRect(10, 8, HUD_WIDTH - 21, HUD_HEIGHT - 17);
    }

    private void drawHealthBar(Graphics graphics, int x, int y, int width, int height) {
        graphics.setColor(new Color(55, 20, 24));
        graphics.fillRect(x, y, width, height);
        if (state != null && state.isStarted()) {
            int maxHp = Math.max(1, state.getPlayer().getMaxHp());
            int hpWidth = Math.max(0, Math.min(width, width * state.getPlayer().getHp() / maxHp));
            graphics.setColor(new Color(190, 50, 62));
            graphics.fillRect(x, y, hpWidth, height);
        }
        graphics.setColor(new Color(235, 230, 210));
        graphics.drawRect(x, y, width, height);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        graphics.drawString(hpText(), x + 8, y + 13);
    }

    private String primaryText() {
        if (state == null || !state.isStarted()) {
            return "Campus Dungeon";
        }
        return "Depth " + state.getDepth()
                + "  |  " + roomText()
                + "  |  " + state.getStatus()
                + "  |  Seed " + state.getSeed();
    }

    private String hpText() {
        if (state == null || !state.isStarted()) {
            return "HP -/-";
        }
        return "HP " + state.getPlayer().getHp() + "/" + state.getPlayer().getMaxHp();
    }

    private String combatText() {
        if (state == null || !state.isStarted()) {
            return "Weapon: keyboard pistol";
        }
        return "Lv " + state.getPlayer().getLevel()
                + "  EXP " + state.getPlayer().getExp()
                + "  ATK/DEF " + state.getPlayer().getAtk() + "/" + state.getPlayer().getDef()
                + "  Weapon " + state.getPlayer().getWeapon()
                + "  Armor " + state.getPlayer().getArmor();
    }

    private String goalText() {
        if (state == null || !state.isStarted()) {
            return "Materials: report, laptop, slides, pass";
        }
        return trimLine("Materials: " + materialProgress()
                + "    Position: (" + state.getPlayer().getX() + ", " + state.getPlayer().getY() + ")"
                + "    Steps: " + state.getPlayer().getSteps()
                + "    Inventory: " + state.getInventory().summary());
    }

    private String roomText() {
        RoomState roomState = state.currentRoomState();
        if (roomState == null) {
            return "Corridor";
        }
        return roomState.getType() + " room " + roomState.getId() + " / " + roomState.getStatus();
    }

    private String materialProgress() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < DEFENSE_MATERIALS.size(); i++) {
            String material = DEFENSE_MATERIALS.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(state.getInventory().contains(material) ? "[x] " : "[ ] ");
            builder.append(material);
        }
        return builder.toString();
    }

    private String messageText() {
        if (state == null) {
            return "Event: Ready.";
        }
        return trimLine("Event: " + state.getMessage());
    }

    private String trimLine(String text) {
        if (text == null || text.length() <= MAX_LINE_CHARS) {
            return text;
        }
        return text.substring(0, MAX_LINE_CHARS - 3) + "...";
    }
}
