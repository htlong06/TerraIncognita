package TerraIncognita.entity.monster;

import TerraIncognita.collision.CollisionManager;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;

/** State machine that drives monster movement and attacks. */
public class MonsterAI {

    private static final double ATTACK_COOLDOWN_SECONDS = 4.0;
    private static final int LEASH_DISTANCE_MULTIPLIER = 2;

    public enum AIState {
        IDLE, CHASE, ATTACK, RETURN_TO_SPAWN
    }

    private AIState aiState;
    private int spawnTileX;
    private int spawnTileY;
    private boolean spawnInitialized;
    private final CollisionManager collisionManager;

    public MonsterAI() {
        this.aiState = AIState.IDLE;
        this.spawnInitialized = false;
        this.collisionManager = new CollisionManager();
    }

    public void update(Monster monster, Player player, GameMap map, double deltaTime) {
        initializeSpawn(monster);

        if (monster.isHurt()) {
            monster.setState(EntityState.HURT);
            return;
        }

        int playerDistance = distanceBetween(monster, player);

        switch (aiState) {
            case IDLE:
                updateIdle(monster, playerDistance);
                break;
            case CHASE:
                updateChase(monster, player, map, deltaTime, playerDistance);
                break;
            case ATTACK:
                updateAttack(monster, player, playerDistance);
                break;
            case RETURN_TO_SPAWN:
                updateReturnToSpawn(monster, map, deltaTime, playerDistance);
                break;
        }
    }

    private void initializeSpawn(Monster monster) {
        if (spawnInitialized) {
            return;
        }

        spawnTileX = monster.getTileX();
        spawnTileY = monster.getTileY();
        spawnInitialized = true;
    }

    private void updateIdle(Monster monster, int playerDistance) {
        monster.setState(EntityState.IDLE);
        if (playerDistance <= monster.getDetectionRange()) {
            becomeAggro(AIState.CHASE, monster);
        }
    }

    private void updateChase(Monster monster, Player player, GameMap map, double deltaTime, int playerDistance) {
        monster.setState(EntityState.WALK);

        if (playerDistance <= monster.getAttackRange()) {
            becomeAggro(AIState.ATTACK, monster);
            return;
        }

        if (playerDistance > getLeashDistance(monster)) {
            aiState = AIState.RETURN_TO_SPAWN;
            monster.setAggro(false);
            return;
        }

        moveTowards(monster, player.getTileX(), player.getTileY(), map, deltaTime);
    }

    private void updateAttack(Monster monster, Player player, int playerDistance) {
        if (playerDistance > monster.getAttackRange()) {
            becomeAggro(AIState.CHASE, monster);
            return;
        }

        Direction attackDirection = getDirectionToPlayer(monster, player);
        monster.setDirection(attackDirection);

        if (monster.getAttackCooldown() > 0) {
            monster.setState(getWaitingState(monster, playerDistance));
            return;
        }

        if (monster.getState() != EntityState.ATTACK || monster.getCurrentAnimation() == null) {
            startAttack(monster, attackDirection);
            return;
        }

        if (monster.getCurrentAnimation().isFinished()) {
            finishAttack(monster, player, playerDistance);
            return;
        }

        monster.setState(EntityState.ATTACK);
    }

    private void updateReturnToSpawn(Monster monster, GameMap map, double deltaTime, int playerDistance) {
        monster.setState(EntityState.WALK);

        if (isAtSpawn(monster)) {
            aiState = AIState.IDLE;
            monster.setAggro(false);
        } else {
            moveTowards(monster, spawnTileX, spawnTileY, map, deltaTime);
        }

        if (playerDistance <= monster.getDetectionRange()) {
            becomeAggro(AIState.CHASE, monster);
        }
    }

    private void startAttack(Monster monster, Direction attackDirection) {
        monster.setState(EntityState.ATTACK);
        monster.resetAttackDamageTriggered();
        monster.resetAnimationForState(EntityState.ATTACK, attackDirection);
    }

    private void finishAttack(Monster monster, Player player, int playerDistance) {
        if (!monster.isAttackDamageTriggered()) {
            int damage = Math.max(1, monster.getAtk() - player.getDef());
            player.takeDamage(damage);
        }

        monster.setAttackCooldown(ATTACK_COOLDOWN_SECONDS);
        monster.resetAttackDamageTriggered();
        monster.setState(getWaitingState(monster, playerDistance));
    }

    private EntityState getWaitingState(Monster monster, int playerDistance) {
        return playerDistance > monster.getDetectionRange() ? EntityState.WALK : EntityState.IDLE;
    }

    private void becomeAggro(AIState nextState, Monster monster) {
        aiState = nextState;
        monster.setAggro(true);
    }

    private void moveTowards(Monster monster, int targetX, int targetY, GameMap map, double deltaTime) {
        int dx = targetX - monster.getTileX();
        int dy = targetY - monster.getTileY();
        Direction direction = choosePrimaryDirection(dx, dy);

        double moveX = direction.getDx() * monster.getSpeed() * deltaTime;
        double moveY = direction.getDy() * monster.getSpeed() * deltaTime;

        double[] resolved = collisionManager.resolveMovement(monster, map, moveX, moveY);
        monster.setWorldX(resolved[0]);
        monster.setWorldY(resolved[1]);
        monster.setDirection(direction);
        monster.updateTilePosition(Constants.TILE_SIZE);
    }

    private Direction getDirectionToPlayer(Monster monster, Player player) {
        int dx = player.getTileX() - monster.getTileX();
        int dy = player.getTileY() - monster.getTileY();
        return choosePrimaryDirection(dx, dy);
    }

    private Direction choosePrimaryDirection(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0 ? Direction.RIGHT : Direction.LEFT;
        }
        return dy >= 0 ? Direction.DOWN : Direction.UP;
    }

    private int distanceBetween(Monster monster, Player player) {
        return manhattanDistance(monster.getTileX(), monster.getTileY(), player.getTileX(), player.getTileY());
    }

    private int manhattanDistance(int startX, int startY, int endX, int endY) {
        return Math.abs(startX - endX) + Math.abs(startY - endY);
    }

    private boolean isAtSpawn(Monster monster) {
        return monster.getTileX() == spawnTileX && monster.getTileY() == spawnTileY;
    }

    private int getLeashDistance(Monster monster) {
        return monster.getDetectionRange() * LEASH_DISTANCE_MULTIPLIER;
    }

    public AIState getAiState() { return aiState; }
}
