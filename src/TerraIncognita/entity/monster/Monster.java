package TerraIncognita.entity.monster;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Entity;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.graphics.Animation;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;

/** Base class for all monsters. */
public abstract class Monster extends Entity {

    private static final double HURT_DURATION_SECONDS = 0.25;

    protected final MonsterAI ai;
    protected int detectionRange;
    protected int expReward;
    protected int goldReward;
    protected boolean aggro;
    protected double attackCooldown;
    protected double hurtTimer;
    protected int attackRange;
    protected boolean attackDamageTriggered;

    public Monster(String name, int hp, int atk, int def, int tileX, int tileY) {
        super();
        this.name = name;
        this.maxHp = hp;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldX = tileX * Constants.TILE_SIZE;
        this.worldY = tileY * Constants.TILE_SIZE;
        this.detectionRange = Constants.DEFAULT_DETECTION_RANGE;
        this.expReward = 10;
        this.goldReward = 5;
        this.aggro = false;
        this.attackCooldown = 0.0;
        this.hurtTimer = 0.0;
        this.attackRange = 1;
        this.attackDamageTriggered = false;
        this.speed = 60;
        this.ai = new MonsterAI();
    }

    @Override
    public void update(double deltaTime) {
        attackCooldown = tickDown(attackCooldown, deltaTime);
        tickHurt(deltaTime);
        updateAnimation(deltaTime);
        updateStatusEffects(deltaTime);
    }

    @Override
    public void takeDamage(int damage) {
        if (!alive) {
            return;
        }

        triggerHurt(damage);
    }

    public void triggerHurt(int damage) {
        if (!alive) {
            return;
        }

        hp = Math.max(0, hp - damage);
        if (hp <= 0) {
            die();
            return;
        }

        enterHurtState();
        playAnimation(EntityState.HURT, direction);
    }

    @Override
    public Rectangle getInteractionBounds() {
        return getHitbox();
    }

    public void updateAI(Player player, GameMap map, double deltaTime) {
        ai.update(this, player, map, deltaTime);
    }

    protected void addDirectionalAnimations(
            EntityState state,
            BufferedImage[] rightFrames,
            BufferedImage[] leftFrames,
            int frameDurationMs,
            boolean looping
    ) {
        if (!hasFrames(rightFrames)) {
            return;
        }

        Animation right = new Animation(rightFrames, frameDurationMs);
        right.setLooping(looping);

        Animation left = new Animation(hasFrames(leftFrames) ? leftFrames : rightFrames, frameDurationMs);
        left.setLooping(looping);

        putAnimation(state, Direction.RIGHT, right);
        putAnimation(state, Direction.LEFT, left);
        putAnimation(state, Direction.UP, right);
        putAnimation(state, Direction.DOWN, right);
    }

    protected void useAnimation(EntityState state, Direction direction) {
        playAnimation(state, direction);
    }

    private void enterHurtState() {
        state = EntityState.HURT;
        hurtTimer = HURT_DURATION_SECONDS;
        if (currentAnimation != null) {
            currentAnimation.reset();
        }
    }

    private void die() {
        hp = 0;
        alive = false;
        state = EntityState.DEAD;
    }

    private void tickHurt(double deltaTime) {
        if (hurtTimer <= 0) {
            return;
        }

        hurtTimer = tickDown(hurtTimer, deltaTime);
        if (hurtTimer == 0 && state == EntityState.HURT) {
            state = EntityState.IDLE;
        }
    }

    private double tickDown(double timer, double deltaTime) {
        return timer > 0 ? Math.max(0, timer - deltaTime) : 0;
    }

    private void playAnimation(EntityState state, Direction direction) {
        Animation anim = animations.get(animationKey(state, direction));
        if (anim == null) {
            anim = animations.get(animationKey(state, Direction.RIGHT));
        }

        if (anim != null) {
            currentAnimation = anim;
            currentAnimation.reset();
        }
    }

    private void putAnimation(EntityState state, Direction direction, Animation animation) {
        animations.put(animationKey(state, direction), animation);
    }

    private String animationKey(EntityState state, Direction direction) {
        return state.name().toLowerCase() + "_" + direction.name().toLowerCase();
    }

    private boolean hasFrames(BufferedImage[] frames) {
        return frames != null && frames.length > 0;
    }

    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }
    public boolean isAggro() { return aggro; }
    public void setAggro(boolean aggro) { this.aggro = aggro; }
    public int getDetectionRange() { return detectionRange; }
    public double getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(double attackCooldown) { this.attackCooldown = attackCooldown; }
    public double getHurtTimer() { return hurtTimer; }
    public boolean isHurt() { return hurtTimer > 0; }
    public boolean isAttackDamageTriggered() { return attackDamageTriggered; }
    public void resetAttackDamageTriggered() { this.attackDamageTriggered = false; }
    public int getAttackRange() { return attackRange; }
    public void setAttackRange(int attackRange) { this.attackRange = Math.max(1, attackRange); }
    public MonsterAI.AIState getAiState() { return ai.getAiState(); }
}
