package TerraIncognita.entity;

import TerraIncognita.graphics.Animation;
import TerraIncognita.item.StatusEffect;
import TerraIncognita.util.Constants;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract class cơ sở cho mọi entity trong game.
 *
 * Entity chỉ giữ dữ liệu (vị trí, chỉ số, animation hiện tại).
 * Renderer đọc dữ liệu từ Entity để vẽ — Entity KHÔNG tự vẽ.
 *
 * Các lớp con: Player, Monster, NPC, Chest...
 */
public abstract class Entity {

    // --- Vị trí ---
    protected double worldX;            // vị trí thực (pixel) trên map
    protected double worldY;
    protected int tileX;                // vị trí ô (tile) trên map
    protected int tileY;
    protected double speed;             // tốc độ di chuyển (pixel/giây)

    // --- Chỉ số ---
    protected int hp;
    protected int maxHp;
    protected int atk;
    protected int def;
    protected String name;

    // --- Trạng thái ---
    protected Direction direction;      // hướng đang quay
    protected EntityState state;        // trạng thái hiện tại (IDLE/WALK/ATTACK...)
    protected boolean alive;

    // --- Animation ---
    protected Map<String, Animation> animations;    // key = "idle_down", "walk_up", "attack_left"...
    protected Animation currentAnimation;

    // --- Hiệu ứng trạng thái ---
    protected List<StatusEffect> activeEffects;

    // --- Hitbox (vùng va chạm), tính lệch so với worldX/worldY ---
    // Mặc định nhỏ hơn 1 tile một chút để va chạm "mượt" hơn, giống cách
    // solidArea được làm nhỏ hơn sprite trong nhiều game 2D top-down.
    protected int hitboxOffsetX;
    protected int hitboxOffsetY;
    protected int hitboxWidth;
    protected int hitboxHeight;

    public Entity() {
        this.worldX = 0;
        this.worldY = 0;
        this.tileX = 0;
        this.tileY = 0;
        this.speed = 0;
        this.hp = 1;
        this.maxHp = 1;
        this.atk = 0;
        this.def = 0;
        this.name = "Entity";
        this.direction = Direction.DOWN;
        this.state = EntityState.IDLE;
        this.alive = true;
        this.animations = new HashMap<>();
        this.currentAnimation = null;
        this.activeEffects = new ArrayList<>();

        // Hitbox mặc định: thu nhỏ 4px mỗi cạnh so với 1 tile
        int inset = 4;
        this.hitboxOffsetX = inset;
        this.hitboxOffsetY = inset;
        this.hitboxWidth = Constants.TILE_SIZE - inset * 2;
        this.hitboxHeight = Constants.TILE_SIZE - inset * 2;
    }

    /**
     * Cập nhật entity mỗi frame.
     * @param deltaTime thời gian (giây) từ frame trước
     */
    public abstract void update(double deltaTime);

    /**
     * Nhận sát thương.
     * @param damage lượng sát thương
     */
    public void takeDamage(int damage) {
        if (!alive) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
            state = EntityState.DEAD;
        }
    }

    /**
     * Hồi máu.
     * @param amount lượng hồi
     */
    public void heal(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }

    /**
     * Cập nhật animation hiện tại dựa trên state + direction.
     */
    protected void updateAnimation(double deltaTime) {
        // Xây key animation: "idle_down", "walk_up"...
        String key = state.name().toLowerCase() + "_" + direction.name().toLowerCase();
        Animation anim = animations.get(key);
        if (anim != null) {
            currentAnimation = anim;
            currentAnimation.update(deltaTime);
        } else {
            System.out.println("[DEBUG Entity.updateAnimation] ANIM NOT FOUND for key='" + key + "'"
                    + " | available keys=" + animations.keySet());
        }
    }

    /**
     * Cập nhật các hiệu ứng trạng thái đang hoạt động.
     */
    protected void updateStatusEffects(double deltaTime) {
        Iterator<StatusEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next();
            effect.update(deltaTime, this);
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }

    /**
     * Thêm hiệu ứng trạng thái.
     */
    public void addStatusEffect(StatusEffect effect) {
        activeEffects.add(effect);
    }

    /**
     * Cập nhật tileX/tileY từ worldX/worldY.
     */
    public void updateTilePosition(int tileSize) {
        this.tileX = (int) (worldX / tileSize);
        this.tileY = (int) (worldY / tileSize);
    }

    /**
     * Tuỳ chỉnh vùng va chạm (hitbox) của entity.
     * Gọi trong constructor của lớp con nếu cần hitbox khác mặc định
     * (ví dụ quái to hơn/nhỏ hơn 1 tile).
     */
    public void setHitbox(int offsetX, int offsetY, int width, int height) {
        this.hitboxOffsetX = offsetX;
        this.hitboxOffsetY = offsetY;
        this.hitboxWidth = width;
        this.hitboxHeight = height;
    }

    /**
     * Hitbox tại vị trí hiện tại (worldX, worldY).
     */
    public Rectangle getHitbox() {
        return getHitboxAt(worldX, worldY);
    }

    /**
     * Hitbox nếu entity đứng tại toạ độ (x, y) — dùng để "thử" va chạm
     * trước khi thật sự di chuyển tới đó (xem CollisionManager).
     */
    public Rectangle getHitboxAt(double x, double y) {
        return new Rectangle(
                (int) Math.round(x) + hitboxOffsetX,
                (int) Math.round(y) + hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
        );
    }

    // --- Getter / Setter ---
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public void setWorldX(double worldX) { this.worldX = worldX; }
    public void setWorldY(double worldY) { this.worldY = worldY; }
    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }
    public void setTileX(int tileX) { this.tileX = tileX; }
    public void setTileY(int tileY) { this.tileY = tileY; }
    public double getSpeed() { return speed; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public boolean isAlive() { return alive; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public EntityState getState() { return state; }
    public void setState(EntityState state) { this.state = state; }
    public Animation getCurrentAnimation() { return currentAnimation; }
    public String getName() { return name; }
    public List<StatusEffect> getActiveEffects() { return activeEffects; }
}
