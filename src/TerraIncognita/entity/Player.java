package TerraIncognita.entity;

import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.inventory.Inventory;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
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
        updateAnimation(deltaTime);
        updateStatusEffects(deltaTime);
        updateTilePosition(Constants.TILE_SIZE);
    }

    /**
     * Di chuyển theo hướng.
     * 
     * @param dir       hướng di chuyển
     * @param deltaTime thời gian frame
     */
    public void move(Direction dir, double deltaTime) {
        this.direction = dir;
        this.state = EntityState.WALK;

        double newX = worldX + dir.getDx() * speed * deltaTime;
        double newY = worldY + dir.getDy() * speed * deltaTime;

        // Kiểm tra va chạm tường trước khi cập nhật vị trí
        if (currentMap != null) {
            int tileSize = Constants.TILE_SIZE;

            // Kiểm tra va chạm theo trục X
            if (dir.getDx() != 0) {
                int checkTileX = (int) ((dir.getDx() > 0 ? newX + tileSize - 1 : newX) / tileSize);
                int checkTileY1 = (int) (worldY / tileSize);
                int checkTileY2 = (int) ((worldY + tileSize - 1) / tileSize);
                if (currentMap.isWalkable(checkTileX, checkTileY1) && currentMap.isWalkable(checkTileX, checkTileY2)) {
                    worldX = newX;
                }
            }

            // Kiểm tra va chạm theo trục Y
            if (dir.getDy() != 0) {
                int checkTileX1 = (int) (worldX / tileSize);
                int checkTileX2 = (int) ((worldX + tileSize - 1) / tileSize);
                int checkTileY = (int) ((dir.getDy() > 0 ? newY + tileSize - 1 : newY) / tileSize);
                if (currentMap.isWalkable(checkTileX1, checkTileY) && currentMap.isWalkable(checkTileX2, checkTileY)) {
                    worldY = newY;
                }
            }
        } else {
            // Không có map → di chuyển tự do
            worldX = newX;
            worldY = newY;
        }

        // Cập nhật tileX, tileY
        updateTilePosition(Constants.TILE_SIZE);
    }

    /**
     * Đặt trạng thái idle khi không di chuyển.
     */
    public void setIdle() {
        if (isAttacking()) {
            return;
        } else {
            this.state = EntityState.IDLE;
        }
    }

    public boolean isAttacking() {
        return attackTimer > 0;
    }

    public boolean canAttack() {
        return attackCoolDownTimer <= 0 && !isAttacking();
    }

    public void stateAttack() {
        this.state = EntityState.ATTACK;
        this.attackTimer = Constants.PLAYER_ATTACK_DURATION;
        this.attackCoolDownTimer = Constants.PLAYER_ATTACK_COOLDOWN;
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

    private void resetAnimations(String key) {
        Animation ani = animations.get(key);
        if (ani != null) {
            ani.reset();
        }
    }

    public void initAnimations(AssetLoader assets) {
        registerDirectionalAnimation(assets, "player_idle", EntityState.IDLE, 130, true);
        registerDirectionalAnimation(assets, "player_walk", EntityState.WALK, 90, true);
        registerDirectionalAnimation(assets, "player_attack", EntityState.ATTACK, 40, false);
        registerDirectionalAnimation(assets, "player_hurt", EntityState.HURT, 90, false);
        registerDirectionalAnimation(assets, "player_dead", EntityState.DEAD, 150, false);
    }
 
    private void registerDirectionalAnimation(AssetLoader assets, String spriteName, EntityState forState, int frameDurationMs, boolean looping) {
        BufferedImage[] facingRight = assets.getFrames(spriteName);
        BufferedImage[] facingLeft = assets.getFramesFlipped(spriteName);
 
        Animation animRight = new Animation(facingRight, frameDurationMs);
        animRight.setLooping(looping);
        Animation animLeft = new Animation(facingLeft, frameDurationMs);
        animLeft.setLooping(looping);
 
        String prefix = forState.name().toLowerCase() + "_";
        animations.put(prefix + "right", animRight);
        animations.put(prefix + "up", animRight);
        animations.put(prefix + "down", animRight);
        animations.put(prefix + "left", animLeft);
    }
}


