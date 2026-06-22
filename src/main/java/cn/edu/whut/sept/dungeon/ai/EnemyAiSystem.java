package cn.edu.whut.sept.dungeon.ai;

import cn.edu.whut.sept.dungeon.combat.CombatSystem;
import cn.edu.whut.sept.dungeon.core.Direction;
import cn.edu.whut.sept.dungeon.core.GameState;
import cn.edu.whut.sept.dungeon.entity.Enemy;
import cn.edu.whut.sept.dungeon.projectile.Projectile;
import cn.edu.whut.sept.dungeon.world.Position;
import cn.edu.whut.sept.dungeon.world.Room;
import cn.edu.whut.sept.dungeon.world.World;

import java.util.ArrayList;
import java.util.List;

public final class EnemyAiSystem {
    private static final int ACTIVE_RANGE = 10;
    private static final int SHOOTER_RANGE = 6;
    private static final int SHOOTER_MIN_DISTANCE = 3;
    private static final int SHOOTER_COOLDOWN_TICKS = 3;
    private static final int ENEMY_PROJECTILE_RANGE = 6;
    private static final int BOSS_PROJECTILE_RANGE = 9;
    private static final int BOSS_PHASE_ONE_COOLDOWN_TICKS = 4;
    private static final int BOSS_PHASE_TWO_COOLDOWN_TICKS = 3;
    private static final int BOSS_PHASE_THREE_COOLDOWN_TICKS = 2;
    private static final int BOSS_MAX_HP = 28;

    public EnemyAiResult tick(World world, List<Enemy> enemies, List<Projectile> projectiles,
                              GameState.PlayerState player, long tick, int activeRoomId) {
        List<Enemy> nextEnemies = new ArrayList<Enemy>();
        List<Projectile> nextProjectiles = new ArrayList<Projectile>(projectiles);
        GameState.PlayerState nextPlayer = player;
        String message = null;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                nextEnemies.add(enemy);
                continue;
            }
            if (!isEnemyAllowedToAct(world, enemy, activeRoomId)) {
                nextEnemies.add(enemy);
                continue;
            }
            if (enemy.isDefenseCommittee()) {
                BossAction action = bossAction(world, enemy, nextPlayer, tick, activeRoomId, nextEnemies, enemies);
                nextProjectiles.addAll(action.projectiles);
                nextEnemies.add(action.enemy);
                nextEnemies.addAll(action.summons);
                if (action.message != null) {
                    message = action.message;
                }
                continue;
            }
            if (enemy.getPosition().manhattanDistanceTo(nextPlayer.getPosition()) == 1) {
                int damage = CombatSystem.damage(enemy.getAtk(), nextPlayer.getDef());
                nextPlayer = nextPlayer.takeDamage(damage);
                nextEnemies.add(enemy);
                message = enemy.getType() + " hits you for " + damage + " damage.";
                continue;
            }
            if (enemy.isReviewShooter()) {
                int distance = enemy.getPosition().manhattanDistanceTo(nextPlayer.getPosition());
                Direction shotDirection = straightShotDirection(enemy.getPosition(), nextPlayer.getPosition());
                if (shotDirection != null && tick % SHOOTER_COOLDOWN_TICKS == 0
                        && distance <= SHOOTER_RANGE) {
                    nextProjectiles.add(Projectile.enemy("enemy-shot-" + tick + "-" + enemy.getId(),
                            enemy.getPosition(), shotDirection, enemy.getAtk(), ENEMY_PROJECTILE_RANGE));
                    nextEnemies.add(enemy);
                    message = enemy.getType() + " fires a review note.";
                    continue;
                }
                if (distance < SHOOTER_MIN_DISTANCE) {
                    nextEnemies.add(moveOneStepAway(world, enemy, nextPlayer.getPosition(), enemies, nextEnemies, activeRoomId));
                } else if (distance > SHOOTER_RANGE) {
                    nextEnemies.add(moveOneStep(world, enemy, nextPlayer.getPosition(), enemies, nextEnemies, activeRoomId));
                } else {
                    nextEnemies.add(enemy);
                }
                continue;
            }
            if (enemy.isDeadlineRunner()) {
                Enemy moved = moveOneStep(world, enemy, nextPlayer.getPosition(), enemies, nextEnemies, activeRoomId);
                if (moved.getPosition().manhattanDistanceTo(nextPlayer.getPosition()) > 1) {
                    moved = moveOneStep(world, moved, nextPlayer.getPosition(), enemies, nextEnemies, activeRoomId);
                }
                nextEnemies.add(moved);
                continue;
            }
            nextEnemies.add(moveOneStep(world, enemy, nextPlayer.getPosition(), enemies, nextEnemies, activeRoomId));
        }

        return new EnemyAiResult(nextPlayer, nextEnemies, nextProjectiles, message);
    }

    private BossAction bossAction(World world, Enemy boss, GameState.PlayerState player, long tick,
                                  int activeRoomId, List<Enemy> alreadyMoved, List<Enemy> allEnemies) {
        List<Projectile> projectiles = new ArrayList<Projectile>();
        List<Enemy> summons = new ArrayList<Enemy>();
        String message = null;
        int phase = bossPhase(boss);
        Direction shotDirection = straightShotDirection(boss.getPosition(), player.getPosition());

        if (phase == 1 && shotDirection != null && tick % BOSS_PHASE_ONE_COOLDOWN_TICKS == 0) {
            projectiles.add(Projectile.enemy("boss-question-" + tick, boss.getPosition(),
                    shotDirection, boss.getAtk(), BOSS_PROJECTILE_RANGE));
            message = "Defense Committee phase 1 asks a direct question.";
        } else if (phase == 2 && tick % BOSS_PHASE_TWO_COOLDOWN_TICKS == 0) {
            Direction primary = shotDirection == null ? preferredDirectionsToward(boss.getPosition(), player.getPosition())[0]
                    : shotDirection;
            Direction[] directions = spreadDirections(primary);
            for (int i = 0; i < directions.length; i++) {
                projectiles.add(Projectile.enemy("boss-triple-" + tick + "-" + i, boss.getPosition(),
                        directions[i], boss.getAtk() + 1, BOSS_PROJECTILE_RANGE));
            }
            message = "Defense Committee phase 2 starts a triple review.";
        } else if (phase == 3 && tick % BOSS_PHASE_THREE_COOLDOWN_TICKS == 0) {
            Direction primary = shotDirection == null ? preferredDirectionsToward(boss.getPosition(), player.getPosition())[0]
                    : shotDirection;
            projectiles.add(Projectile.enemy("boss-final-" + tick, boss.getPosition(),
                    primary, boss.getAtk() + 2, BOSS_PROJECTILE_RANGE));
            Enemy summon = bossSummon(world, boss, activeRoomId, alreadyMoved, allEnemies, tick);
            if (summon != null) {
                summons.add(summon);
                message = "Defense Committee phase 3 summons final questions.";
            } else {
                message = "Defense Committee phase 3 launches final questions.";
            }
        }

        Enemy movedBoss = boss;
        if (boss.getPosition().manhattanDistanceTo(player.getPosition()) < SHOOTER_MIN_DISTANCE) {
            movedBoss = moveOneStepAway(world, boss, player.getPosition(), allEnemies, alreadyMoved, activeRoomId);
        }
        return new BossAction(movedBoss, projectiles, summons, message);
    }

    private int bossPhase(Enemy boss) {
        if (boss.getHp() <= BOSS_MAX_HP / 4) {
            return 3;
        }
        if (boss.getHp() <= BOSS_MAX_HP / 2) {
            return 2;
        }
        return 1;
    }

    private Direction[] spreadDirections(Direction primary) {
        if (primary == Direction.NORTH || primary == Direction.SOUTH) {
            return new Direction[]{primary, Direction.WEST, Direction.EAST};
        }
        return new Direction[]{primary, Direction.NORTH, Direction.SOUTH};
    }

    private Enemy bossSummon(World world, Enemy boss, int activeRoomId, List<Enemy> alreadyMoved,
                             List<Enemy> allEnemies, long tick) {
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction direction : directions) {
            Position candidate = new Position(boss.getPosition().getX() + direction.getDx(),
                    boss.getPosition().getY() + direction.getDy());
            if (canMoveTo(world, candidate, boss.getPosition(), boss, allEnemies, alreadyMoved, activeRoomId)) {
                return Enemy.bug("boss-summon-" + tick, candidate);
            }
        }
        return null;
    }

    private boolean isEnemyAllowedToAct(World world, Enemy enemy, int activeRoomId) {
        if (activeRoomId < 0) {
            return false;
        }
        return roomIdAt(world, enemy.getPosition()) == activeRoomId;
    }

    private Enemy moveOneStep(World world, Enemy enemy, Position player, List<Enemy> allEnemies,
                              List<Enemy> alreadyMoved, int activeRoomId) {
        if (enemy.getPosition().manhattanDistanceTo(player) > ACTIVE_RANGE) {
            return enemy;
        }
        Direction[] directions = preferredDirectionsToward(enemy.getPosition(), player);
        for (Direction direction : directions) {
            Position candidate = new Position(enemy.getPosition().getX() + direction.getDx(),
                    enemy.getPosition().getY() + direction.getDy());
            if (canMoveTo(world, candidate, player, enemy, allEnemies, alreadyMoved, activeRoomId)) {
                return enemy.moveTo(candidate);
            }
        }
        return enemy;
    }

    private Enemy moveOneStepAway(World world, Enemy enemy, Position player, List<Enemy> allEnemies,
                                  List<Enemy> alreadyMoved, int activeRoomId) {
        Direction[] directions = preferredDirectionsAway(enemy.getPosition(), player);
        for (Direction direction : directions) {
            Position candidate = new Position(enemy.getPosition().getX() + direction.getDx(),
                    enemy.getPosition().getY() + direction.getDy());
            if (canMoveTo(world, candidate, player, enemy, allEnemies, alreadyMoved, activeRoomId)
                    && candidate.manhattanDistanceTo(player) > enemy.getPosition().manhattanDistanceTo(player)) {
                return enemy.moveTo(candidate);
            }
        }
        return enemy;
    }

    private boolean canMoveTo(World world, Position candidate, Position player, Enemy movingEnemy,
                              List<Enemy> allEnemies, List<Enemy> alreadyMoved, int activeRoomId) {
        if (!world.contains(candidate.getX(), candidate.getY()) || !world.isWalkable(candidate)) {
            return false;
        }
        if (candidate.equals(player)) {
            return false;
        }
        if (roomIdAt(world, candidate) != activeRoomId) {
            return false;
        }
        for (Enemy enemy : alreadyMoved) {
            if (enemy.isAlive() && enemy.getPosition().equals(candidate)) {
                return false;
            }
        }
        for (Enemy enemy : allEnemies) {
            if (enemy.isAlive()
                    && !enemy.getId().equals(movingEnemy.getId())
                    && enemy.getPosition().equals(candidate)
                    && !alreadyContains(alreadyMoved, enemy.getId())) {
                return false;
            }
        }
        return true;
    }

    private boolean alreadyContains(List<Enemy> alreadyMoved, String enemyId) {
        for (Enemy enemy : alreadyMoved) {
            if (enemy.getId().equals(enemyId)) {
                return true;
            }
        }
        return false;
    }

    private Direction straightShotDirection(Position from, Position target) {
        if (from.getX() == target.getX()) {
            return target.getY() < from.getY() ? Direction.NORTH : Direction.SOUTH;
        }
        if (from.getY() == target.getY()) {
            return target.getX() < from.getX() ? Direction.WEST : Direction.EAST;
        }
        return null;
    }

    private Direction[] preferredDirectionsToward(Position from, Position target) {
        Direction horizontal = target.getX() < from.getX() ? Direction.WEST : Direction.EAST;
        Direction vertical = target.getY() < from.getY() ? Direction.NORTH : Direction.SOUTH;
        if (Math.abs(target.getX() - from.getX()) >= Math.abs(target.getY() - from.getY())) {
            return new Direction[]{horizontal, vertical, opposite(vertical), opposite(horizontal)};
        }
        return new Direction[]{vertical, horizontal, opposite(horizontal), opposite(vertical)};
    }

    private Direction[] preferredDirectionsAway(Position from, Position target) {
        Direction towardHorizontal = target.getX() < from.getX() ? Direction.WEST : Direction.EAST;
        Direction towardVertical = target.getY() < from.getY() ? Direction.NORTH : Direction.SOUTH;
        Direction horizontal = opposite(towardHorizontal);
        Direction vertical = opposite(towardVertical);
        if (Math.abs(target.getX() - from.getX()) >= Math.abs(target.getY() - from.getY())) {
            return new Direction[]{horizontal, vertical, opposite(vertical), opposite(horizontal)};
        }
        return new Direction[]{vertical, horizontal, opposite(horizontal), opposite(vertical)};
    }

    private Direction opposite(Direction direction) {
        switch (direction) {
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case WEST:
                return Direction.EAST;
            case EAST:
                return Direction.WEST;
            default:
                throw new IllegalArgumentException("Unsupported direction: " + direction);
        }
    }

    private int roomIdAt(World world, Position position) {
        for (int i = 0; i < world.getRooms().size(); i++) {
            Room room = world.getRooms().get(i);
            if (room.contains(position)) {
                return i;
            }
        }
        return -1;
    }

    private static final class BossAction {
        private final Enemy enemy;
        private final List<Projectile> projectiles;
        private final List<Enemy> summons;
        private final String message;

        private BossAction(Enemy enemy, List<Projectile> projectiles, List<Enemy> summons, String message) {
            this.enemy = enemy;
            this.projectiles = projectiles;
            this.summons = summons;
            this.message = message;
        }
    }
}
