package TerraIncognita.entity.monster;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;

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

    public MonsterAI() {
        this.aiState = AIState.IDLE;
    }

    public void update(Monster monster, Player player, GameMap map, double deltaTime) {
        // Lưu spawn nếu chưa có
        if (spawnTileX == 0 && spawnTileY == 0) {
            spawnTileX = monster.getTileX();
            spawnTileY = monster.getTileY();
        }

        int dist = manhattanDistance(
            monster.getTileX(), monster.getTileY(),
            player.getTileX(), player.getTileY()
        );

        switch (aiState) {
            case IDLE:
                if (dist <= monster.getDetectionRange()) {
                    aiState = AIState.CHASE;
                    monster.setAggro(true);
                }
                break;
            case CHASE:
                if (dist <= 1) {
                    aiState = AIState.ATTACK;
                } else if (dist > monster.getDetectionRange() * 2) {
                    aiState = AIState.RETURN_TO_SPAWN;
                    monster.setAggro(false);
                } else {
                    // Di chuyển đơn giản về phía player
                    moveTowards(monster, player.getTileX(), player.getTileY(), map, deltaTime);
                }
                break;
            case ATTACK:
                // TODO (GĐ4): Gây damage cho player qua CombatSystem
                if (dist > 1) {
                    aiState = AIState.CHASE;
                }
                break;
            case RETURN_TO_SPAWN:
                if (monster.getTileX() == spawnTileX && monster.getTileY() == spawnTileY) {
                    aiState = AIState.IDLE;
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

        // TODO: Kiểm tra va chạm trước khi di chuyển
        double newX = monster.getWorldX() + dir.getDx() * monster.getSpeed() * deltaTime;
        double newY = monster.getWorldY() + dir.getDy() * monster.getSpeed() * deltaTime;
        monster.setWorldX(newX);
        monster.setWorldY(newY);
        monster.setDirection(dir);
        monster.updateTilePosition(32);  // TODO: dùng Constants.TILE_SIZE
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public AIState getAiState() { return aiState; }
}
