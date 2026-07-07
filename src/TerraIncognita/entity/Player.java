package TerraIncognita.entity;

import TerraIncognita.inventory.Inventory;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
import java.util.EnumMap;
import java.util.Map;

/**
 * Nhân vật người chơi.
 *
 * Thuộc tính mở rộng so với Entity:
 * - Level, EXP
 * - Inventory (túi đồ)
 * - Equipment (trang bị đang mang)
 * - Gold (vàng)
 */
public class Player extends Entity {

    private int level;
    private int exp;
    private int expToNextLevel;     // EXP cần để lên level tiếp
    private int gold;
    private Inventory inventory;
    private Map<EquipmentSlot, Equipment> equippedItems;

    // Tham chiếu tới map hiện tại để kiểm tra va chạm
    private GameMap currentMap;

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
        // Cập nhật animation
        updateAnimation(deltaTime);
        // Cập nhật status effects
        updateStatusEffects(deltaTime);
        // Cập nhật tileX/tileY
        updateTilePosition(Constants.TILE_SIZE);
    }

    /**
     * Di chuyển theo hướng.
     * @param dir hướng di chuyển
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
        this.state = EntityState.IDLE;
    }

    /**
     * Thêm EXP, kiểm tra lên level.
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
        hp = maxHp;     // Hồi đầy HP khi lên level
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
     */
    public void equip(Equipment equipment) {
        EquipmentSlot slot = equipment.getSlot();

        // Tháo trang bị cũ nếu có
        Equipment old = equippedItems.get(slot);
        if (old != null) {
            atk -= old.getAtkBonus();
            def -= old.getDefBonus();
            inventory.addItem(old);
        }

        // Đặt trang bị mới
        equippedItems.put(slot, equipment);
        atk += equipment.getAtkBonus();
        def += equipment.getDefBonus();
        inventory.removeItem(equipment);
    }

    /**
     * Gán bản đồ hiện tại (dùng cho va chạm).
     */
    public void setCurrentMap(GameMap map) {
        this.currentMap = map;
    }

    // --- Getter ---
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getExpToNextLevel() { return expToNextLevel; }
    public int getGold() { return gold; }
    public Inventory getInventory() { return inventory; }
    public Map<EquipmentSlot, Equipment> getEquippedItems() { return equippedItems; }
    public GameMap getCurrentMap() { return currentMap; }
}
