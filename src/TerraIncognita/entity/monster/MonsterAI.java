package TerraIncognita.entity.monster;

import TerraIncognita.collision.CollisionManager;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;

/**
 * Logic AI cho quái vật.
 * Hành vi: IDLE → CHASE → ATTACK → RETURN
 */
public class MonsterAI {

    public enum AIState {
        IDLE, CHASE, ATTACK, RETURN_TO_SPAWN
    }

    private AIState aiState;
    private int spawnTileX, spawnTileY;
    private boolean spawnInitialized;
    private final CollisionManager collisionManager;

    public MonsterAI() {
        this.aiState = AIState.IDLE;
        this.spawnInitialized = false;
        this.collisionManager = new CollisionManager();
    }

    public void update(Monster monster, Player player, GameMap map, double deltaTime) {
        if (!spawnInitialized) {
            spawnTileX = monster.getTileX();
            spawnTileY = monster.getTileY();
            spawnInitialized = true;
        }

        int dist = manhattanDistance(
            monster.getTileX(), monster.getTileY(),
            player.getTileX(), player.getTileY()
        );

        switch (aiState) {
            case IDLE:
                monster.setState(TerraIncognita.entity.EntityState.IDLE);
                if (dist <= monster.getDetectionRange()) {
                    aiState = AIState.CHASE;
                    monster.setAggro(true);
                }
                break;
            case CHASE:
                monster.setState(TerraIncognita.entity.EntityState.WALK);
                if (dist <= 1) {
                    aiState = AIState.ATTACK;
                    monster.setAggro(true);
                } else if (dist > monster.getDetectionRange() * 2) {
                    aiState = AIState.RETURN_TO_SPAWN;
                    monster.setAggro(false);
                } else {
                    moveTowards(monster, player.getTileX(), player.getTileY(), map, deltaTime);
                }
                break;
            case ATTACK:
                if (dist > 1) {
                    aiState = AIState.CHASE;
                    monster.setAggro(true);
                    break;
                }

                Direction attackDir = getAttackDirection(monster, player);
                if (monster.getAttackCooldown() <= 0) {
                    monster.setState(TerraIncognita.entity.EntityState.ATTACK);
                    monster.setDirection(attackDir);
                    monster.resetAnimationForState(TerraIncognita.entity.EntityState.ATTACK, attackDir);
                    int rawDamage = Math.max(1, monster.getAtk() - player.getDef());
                    player.takeDamage(rawDamage);
                    monster.setAttackCooldown(4.0);
                } else {
                    monster.setState(TerraIncognita.entity.EntityState.ATTACK);
                    monster.setDirection(attackDir);
                }
                break;
            case RETURN_TO_SPAWN:
                monster.setState(TerraIncognita.entity.EntityState.WALK);
                if (monster.getTileX() == spawnTileX && monster.getTileY() == spawnTileY) {
                    aiState = AIState.IDLE;
                    monster.setAggro(false);
                } else {
                    moveTowards(monster, spawnTileX, spawnTileY, map, deltaTime);
                }
                if (dist <= monster.getDetectionRange()) {
                    aiState = AIState.CHASE;
                    monster.setAggro(true);
                }
                break;
        }
    }

    private void moveTowards(Monster monster, int targetX, int targetY, GameMap map, double deltaTime) {
        int dx = targetX - monster.getTileX();
        int dy = targetY - monster.getTileY();

        Direction dir = null;
        if (Math.abs(dx) >= Math.abs(dy)) {
            dir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            dir = dy > 0 ? Direction.DOWN : Direction.UP;
        }

        double moveX = dir.getDx() * monster.getSpeed() * deltaTime;
        double moveY = dir.getDy() * monster.getSpeed() * deltaTime;

        // Va chạm tường được CollisionManager xử lý bằng hitbox, tách
        // trục X/Y để quái có thể trượt dọc tường thay vì bị kẹt cứng.
        double[] resolved = collisionManager.resolveMovement(monster, map, moveX, moveY);
        monster.setWorldX(resolved[0]);
        monster.setWorldY(resolved[1]);
        monster.setDirection(dir);
        monster.updateTilePosition(Constants.TILE_SIZE);
    }

    private Direction getAttackDirection(Monster monster, Player player) {
        int dx = player.getTileX() - monster.getTileX();
        int dy = player.getTileY() - monster.getTileY();

        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0 ? Direction.RIGHT : Direction.LEFT;
        }
        return dy >= 0 ? Direction.DOWN : Direction.UP;
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public AIState getAiState() { return aiState; }
}
