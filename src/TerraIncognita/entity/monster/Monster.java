package TerraIncognita.entity.monster;

import java.awt.Rectangle;

import TerraIncognita.entity.Entity;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.graphics.Animation;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;

/**
 * Abstract class quái vật cơ sở.
 * Mỗi loại quái kế thừa và override chỉ số + hành vi riêng.
 */
public abstract class Monster extends Entity {

    protected MonsterAI ai;
    protected int detectionRange;
    protected int expReward;
    protected int goldReward;
    protected boolean aggro;
    protected double attackCooldown;
    protected double hurtTimer;
    protected int attackRange;

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
        this.speed = 60;   // Quái di chuyển chậm hơn player
        this.ai = new MonsterAI();
    }

    @Override
    public void update(double deltaTime) {
        if (attackCooldown > 0) {
            attackCooldown -= deltaTime;
            if (attackCooldown < 0) {
                attackCooldown = 0;
            }
        }

        if (hurtTimer > 0) {
            hurtTimer -= deltaTime;
            if (hurtTimer <= 0) {
                hurtTimer = 0;
                if (state == EntityState.HURT) {
                    state = EntityState.IDLE;
                }
            }
        }

        updateAnimation(deltaTime);
        updateStatusEffects(deltaTime);
    }

    @Override
    public void takeDamage(int damage) {
        if (!alive) return;
        triggerHurt(damage);
    }

    public void triggerHurt(int damage) {
        if (!alive) return;

        hp = Math.max(0, hp - damage);
        if (hp <= 0) {
            hp = 0;
            alive = false;
            state = EntityState.DEAD;
            return;
        }

        state = EntityState.HURT;
        hurtTimer = 0.25;
        if (currentAnimation != null) {
            currentAnimation.reset();
        }

        String key = state.name().toLowerCase() + "_" + direction.name().toLowerCase();
        Animation anim = animations.get(key);
        if (anim == null) {
            anim = animations.get(state.name().toLowerCase() + "_right");
        }
        if (anim != null) {
            currentAnimation = anim;
            currentAnimation.reset();
        }
    }

    /**
     * Vùng tương tác mặc định của quái — dùng hitbox (vùng va chạm).
     * Quái không có "tương tác" theo nghĩa mở rương/nói chuyện, nhưng
     * phải cài đặt phương thức trừu tượng từ Entity.
     * @return Rectangle hitbox tại vị trí hiện tại
     */
    @Override
    public Rectangle getInteractionBounds() {
        return getHitbox();
    }

    /**
     * Cập nhật AI quái (cần biết vị trí player và map).
     */
    public void updateAI(Player player, GameMap map, double deltaTime) {
        ai.update(this, player, map, deltaTime);
    }

    // --- Getter ---
    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }
    public boolean isAggro() { return aggro; }
    public void setAggro(boolean aggro) { this.aggro = aggro; }
    public int getDetectionRange() { return detectionRange; }
    public double getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(double attackCooldown) { this.attackCooldown = attackCooldown; }
    public int getAttackRange() { return attackRange; }
    public void setAttackRange(int attackRange) { this.attackRange = Math.max(1, attackRange); }
    public MonsterAI.AIState getAiState() { return ai.getAiState(); }
}
