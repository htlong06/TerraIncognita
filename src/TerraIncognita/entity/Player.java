package TerraIncognita.entity;

import TerraIncognita.collision.CollisionManager;
import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.inventory.Inventory;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.Map;
import java.awt.image.BufferedImage;

public class Player extends Entity {

    private int level;
    private int exp;
    private int expToNextLevel;
    private int gold;
    private Inventory inventory;
    private Map<EquipmentSlot, Equipment> equippedItems;

    // Tham chiếu tới map hiện tại để kiểm tra va chạm
    private GameMap currentMap;

    // Xử lý va chạm (tile + entity + bẫy), tách riêng khỏi Player
    private CollisionManager collisionManager = new CollisionManager();

    private double attackTimer;
    private double attackCoolDownTimer;

    public Player() {
        super();
        this.name = "Player";
        this.maxHp = Constants.PLAYER_START_HP;
        this.hp = maxHp;
        this.atk = Constants.PLAYER_START_ATK;
        this.def = Constants.PLAYER_START_DEF;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.DOWN;
        this.state = EntityState.IDLE;

        this.level = 1;
        this.exp = 0;
        this.expToNextLevel = 100;
        this.gold = 0;
        this.inventory = new Inventory(Constants.INVENTORY_MAX_SLOTS);
        this.equippedItems = new EnumMap<>(EquipmentSlot.class);
    }

    @Override
    public void update(double deltaTime) {
        System.out.println("[DEBUG Player.update] START — state=" + state + " dir=" + direction
                + " attackTimer=" + String.format("%.3f", attackTimer)
                + " cooldown=" + String.format("%.3f", attackCoolDownTimer)
                + " isAttacking=" + isAttacking());
        updateAnimation(deltaTime);
        updateStatusEffects(deltaTime);
        updateTilePosition(Constants.TILE_SIZE);
        updateAttackTimers(deltaTime);
        System.out.println("[DEBUG Player.update] END   — state=" + state
                + " attackTimer=" + String.format("%.3f", attackTimer)
                + " currentAnim=" + (currentAnimation != null ? currentAnimation.getFrameCount() + "frames" : "NULL"));
    }

    /**
     * Đếm ngược thời gian đòn đánh hiện tại + thời gian hồi chiêu.
     * (Trước đây 2 timer này được set nhưng không bao giờ giảm, khiến
     * isAttacking() luôn true sau nhát chém đầu tiên — sửa lại ở đây.)
     */
    private void updateAttackTimers(double deltaTime) {
        if (attackTimer > 0) {
            double before = attackTimer;
            attackTimer -= deltaTime;
            if (attackTimer < 0) {
                attackTimer = 0;
            }
            System.out.println("[DEBUG attackTimer] " + String.format("%.3f", before)
                    + " -> " + String.format("%.3f", attackTimer)
                    + " (dt=" + String.format("%.3f", deltaTime) + ")");
        }
        if (attackCoolDownTimer > 0) {
            attackCoolDownTimer -= deltaTime;
            if (attackCoolDownTimer < 0) {
                attackCoolDownTimer = 0;
            }
        }
    }

    /**
     * Di chuyển theo hướng.
     * 
     * @param dir       hướng di chuyển
     * @param deltaTime thời gian frame
     */
    public void move(Direction dir, double deltaTime) {
        this.direction = dir;
        if (!isAttacking()) {
            this.state = EntityState.WALK;
        } else {
            System.out.println("[DEBUG Player.move] BLOCKED state change to WALK — isAttacking=true, keeping state=" + state);
        }

        double dx = dir.getDx() * speed * deltaTime;
        double dy = dir.getDy() * speed * deltaTime;

        if (currentMap != null) {
            // Va chạm tường được xử lý bởi CollisionManager (dựa trên
            // hitbox, tách trục X/Y để trượt dọc tường khi đi chéo).
            double[] resolved = collisionManager.resolveMovement(this, currentMap, dx, dy);
            worldX = resolved[0];
            worldY = resolved[1];
        } else {
            // Không có map → di chuyển tự do
            worldX = worldX + dx;
            worldY = worldY + dy;
        }

        // Cập nhật tileX, tileY
        updateTilePosition(Constants.TILE_SIZE);
    }

    /**
     * Đặt trạng thái idle khi không di chuyển.
     */
    public void setIdle() {
        if (isAttacking()) {
            System.out.println("[DEBUG Player.setIdle] BLOCKED — isAttacking=true, keeping state=" + state);
            return;
        } else {
            System.out.println("[DEBUG Player.setIdle] state -> IDLE");
            this.state = EntityState.IDLE;
        }
    }

    public boolean isAttacking() {
        return attackTimer > 0;
    }

    public boolean canAttack() {
        boolean can = attackCoolDownTimer <= 0 && !isAttacking();
        System.out.println("[DEBUG Player.canAttack] cooldown=" + String.format("%.3f", attackCoolDownTimer)
                + " isAttacking=" + isAttacking() + " => canAttack=" + can);
        return can;
    }

    public void stateAttack() {
        System.out.println("[DEBUG Player.stateAttack] === ATTACK TRIGGERED ===");
        System.out.println("[DEBUG Player.stateAttack] state: " + this.state + " -> ATTACK");
        System.out.println("[DEBUG Player.stateAttack] attackTimer: " + String.format("%.3f", attackTimer)
                + " -> " + Constants.PLAYER_ATTACK_DURATION);
        System.out.println("[DEBUG Player.stateAttack] cooldown: " + String.format("%.3f", attackCoolDownTimer)
                + " -> " + Constants.PLAYER_ATTACK_COOLDOWN);
        this.state = EntityState.ATTACK;
        this.attackTimer = Constants.PLAYER_ATTACK_DURATION;
        this.attackCoolDownTimer = Constants.PLAYER_ATTACK_COOLDOWN;

        // Reset attack animation về frame 0 để chạy lại từ đầu.
        // (Attack animation có looping=false, nên sau lần đầu finished=true
        //  và kẹt ở frame cuối — phải reset thủ công mỗi lần tấn công.)
        for (String dir : new String[]{"right", "left", "up", "down"}) {
            Animation anim = animations.get("attack_" + dir);
            if (anim != null) {
                anim.reset();
            }
        }
    }

    /**
     * Vùng va chạm của nhát chém hiện tại — 1 hình chữ nhật nhô ra phía
     * trước player theo hướng đang quay mặt (xem
     * CollisionManager#getAttackHitbox). Hiện chỉ có vũ khí cận chiến
     * (kiếm) nên dùng chung 1 tầm đánh Constants.PLAYER_ATTACK_RANGE;
     * sau này nếu thêm vũ khí tầm xa thì đổi range theo currentWeapon.
     */
    public Rectangle getAttackHitbox() {
        return collisionManager.getAttackHitbox(this, Constants.PLAYER_ATTACK_RANGE);
    }

    /**
     * Thêm EXP, kiểm tra lên level.
     * 
     * @param amount lượng EXP nhận
     */
    public void addExp(int amount) {
        exp += amount;
        while (exp >= expToNextLevel) {
            levelUp();
        }
    }

    /**
     * Lên level.
     */
    private void levelUp() {
        exp -= expToNextLevel;
        level++;
        // Tăng chỉ số
        maxHp += 10;
        hp = maxHp; // Hồi đầy HP khi lên level
        atk += 2;
        def += 1;
        // Tăng EXP cần thiết cho level tiếp theo
        expToNextLevel = (int) (expToNextLevel * 1.5);
    }

    /**
     * Thêm vàng.
     */
    public void addGold(int amount) {
        gold += amount;
    }

    /**
     * Tiêu vàng.
     * 
     * @return true nếu đủ vàng để tiêu
     */
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    /**
     * Trang bị item.
     * 
     * @return true nếu trang bị thành công
     */
    public boolean equip(Equipment equipment) {
        EquipmentSlot slot = equipment.getSlot();
        // Must remove from inventory first
        if (!inventory.removeItem(equipment)) {
            return false;
        }
        // Try to return old equipment
        Equipment old = equippedItems.get(slot);
        if (old != null) {
            if (inventory.isFull()) {
                // Cannot return old — rollback: put equipment back in inventory
                inventory.addItem(equipment);
                return false;
            }
            atk -= old.getAtkBonus();
            def -= old.getDefBonus();
            inventory.addItem(old);
        }
        equippedItems.put(slot, equipment);
        atk += equipment.getAtkBonus();
        def += equipment.getDefBonus();
        return true;
    }

    /**
     * Gán bản đồ hiện tại (dùng cho va chạm).
     */
    public void setCurrentMap(GameMap map) {
        this.currentMap = map;
    }

    /**
     * Cho phép GameEngine truyền vào 1 CollisionManager dùng chung
     * (thay vì mỗi Player tự tạo 1 cái riêng).
     */
    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    // --- Getter ---
    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public int getExpToNextLevel() {
        return expToNextLevel;
    }

    public int getGold() {
        return gold;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Map<EquipmentSlot, Equipment> getEquippedItems() {
        return equippedItems;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    // --- Setter (dùng cho load game) ---
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setAtk(int atk) { this.atk = atk; }
    public void setDef(int def) { this.def = def; }
    public void setLevel(int level) { this.level = level; }
    public void setExp(int exp) { this.exp = exp; }
    public void setExpToNextLevel(int expToNextLevel) { this.expToNextLevel = expToNextLevel; }
    public void setGold(int gold) { this.gold = gold; }

    private void resetAnimations(String key) {
        Animation ani = animations.get(key);
        if (ani != null) {
            ani.reset();
        }
    }

    public void initAnimations(AssetLoader assets) {
        System.out.println("[DEBUG Player.initAnimations] === REGISTERING ALL ANIMATIONS ===");
        registerDirectionalAnimation(assets, "player_idle", EntityState.IDLE, 130, true);
        registerDirectionalAnimation(assets, "player_walk", EntityState.WALK, 90, true);
        registerDirectionalAnimation(assets, "player_attack", EntityState.ATTACK, 40, false);
        registerDirectionalAnimation(assets, "player_hurt", EntityState.HURT, 90, false);
        registerDirectionalAnimation(assets, "player_dead", EntityState.DEAD, 150, false);
        System.out.println("[DEBUG Player.initAnimations] All animation keys: " + animations.keySet());
    }
 
    private void registerDirectionalAnimation(AssetLoader assets, String spriteName, EntityState forState, int frameDurationMs, boolean looping) {
        BufferedImage[] facingRight = assets.getFrames(spriteName);
        BufferedImage[] facingLeft = assets.getFramesFlipped(spriteName);
 
        System.out.println("[DEBUG registerAnim] sprite='" + spriteName + "' state=" + forState
                + " framesRight=" + facingRight.length + " framesLeft=" + facingLeft.length
                + " durationMs=" + frameDurationMs + " loop=" + looping);
 
        Animation animRight = new Animation(facingRight, frameDurationMs);
        animRight.setLooping(looping);
        Animation animLeft = new Animation(facingLeft, frameDurationMs);
        animLeft.setLooping(looping);
 
        String prefix = forState.name().toLowerCase() + "_";
        animations.put(prefix + "right", animRight);
        animations.put(prefix + "up", animRight);
        animations.put(prefix + "down", animRight);
        animations.put(prefix + "left", animLeft);
        System.out.println("[DEBUG registerAnim] Registered keys: " + prefix + "{right,up,down,left}");
    }
}


