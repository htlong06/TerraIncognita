package TerraIncognita.entity;

import TerraIncognita.economy.LootTable;
import TerraIncognita.item.Item;
import TerraIncognita.util.Constants;

/**
 * Rương báu / kho báu.
 * Là Entity đứng yên, khi player tương tác sẽ mở ra và sinh item ngẫu nhiên.
 */
public class Chest extends Entity {

    private boolean opened;
    private boolean locked;
    private String requiredKeyId;
    private LootTable lootTable;
    private Item lastLoot;  // item vừa nhặt từ lần open() gần nhất

    public Chest(int tileX, int tileY, boolean locked) {
        super();
        this.name = "Chest";
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldX = tileX * Constants.TILE_SIZE;
        this.worldY = tileY * Constants.TILE_SIZE;
        this.opened = false;
        this.locked = locked;
        this.requiredKeyId = "";
        this.speed = 0;
    }

    @Override
    public void update(double deltaTime) {
        updateAnimation(deltaTime);
    }

    /**
     * Mở rương. Kiểm tra khóa, sinh item.
     * @param player người chơi đang tương tác
     * @return true nếu mở thành công
     */
    public boolean open(Player player) {
        if (opened) return false;
        if (locked && !requiredKeyId.isEmpty()) {
            // Kiểm tra player có key không
            if (player.getInventory().hasItem(requiredKeyId)) {
                player.getInventory().removeItem(player.getInventory().findById(requiredKeyId));
                locked = false;
            } else {
                return false;
            }
        }
        opened = true;
        lastLoot = null;
        // Sinh item từ loot table vào player inventory
        if (lootTable != null) {
            Item loot = lootTable.generateLoot();
            if (loot != null) {
                player.getInventory().addItem(loot);
                lastLoot = loot;
            }
        }
        return true;
    }

    // --- Getter ---
    public boolean isOpened() { return opened; }
    public boolean isLocked() { return locked; }
    public Item getLastLoot() { return lastLoot; }
    public void setRequiredKeyId(String keyId) { this.requiredKeyId = keyId; }
    public void setLootTable(LootTable table) { this.lootTable = table; }
}
