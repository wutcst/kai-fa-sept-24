package cn.edu.whut.sept.dungeon.room;

import cn.edu.whut.sept.dungeon.world.Room;
import cn.edu.whut.sept.dungeon.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RoomState {
    private final int id;
    private final RoomType type;
    private final RoomStatus status;
    private final boolean rewardCreated;

    public RoomState(int id, RoomType type, RoomStatus status, boolean rewardCreated) {
        this.id = id;
        this.type = type == null ? RoomType.COMBAT : type;
        this.status = status == null ? RoomStatus.LOCKED : status;
        this.rewardCreated = rewardCreated;
    }

    public static List<RoomState> createFor(World world) {
        if (world == null) {
            return Collections.emptyList();
        }
        List<RoomState> result = new ArrayList<RoomState>();
        for (int i = 0; i < world.getRooms().size(); i++) {
            result.add(new RoomState(i, RoomType.REWARD, RoomStatus.COMPLETED, false));
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public RoomType getType() {
        return type;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public boolean isRewardCreated() {
        return rewardCreated;
    }

    public boolean isCombatLike() {
        return type == RoomType.COMBAT || type == RoomType.BOSS;
    }

    public boolean isLockedForCombat() {
        return isCombatLike() && status == RoomStatus.ACTIVE;
    }

    public RoomState activate() {
        if (!isCombatLike() || status != RoomStatus.LOCKED) {
            return this;
        }
        return new RoomState(id, type, RoomStatus.ACTIVE, rewardCreated);
    }

    public RoomState clear() {
        if (!isCombatLike() || status == RoomStatus.CLEARED || status == RoomStatus.COMPLETED) {
            return this;
        }
        return new RoomState(id, type, RoomStatus.CLEARED, rewardCreated);
    }

    public RoomState withRewardCreated() {
        return new RoomState(id, type, status, true);
    }
}
