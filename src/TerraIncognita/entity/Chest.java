package TerraIncognita.entity;

import TerraIncognita.economy.LootTable;
import TerraIncognita.item.Item;
import TerraIncognita.util.Constants;

/**
 * Rương báu / kho báu.
 * Là Entity đứng yên, khi player va chạm sẽ mở ra và sinh item ngẫu nhiên.
 */
public class Chest extends Entity {

    private boolean opened;
    private LootTable lootTable;
    private Item lastLoot;  // item vừa nhặt từ lần open() gần nhất
    private String rarity; // "common", "rare", "mythic"

    public Chest(int tileX, int tileY, String rarity) {
        super();
        this.name = "Chest";
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldX = tileX * Constants.TILE_SIZE;
        this.worldY = tileY * Constants.TILE_SIZE;
        this.opened = false;
        this.speed = 0;
        this.rarity = rarity;
    }

    @Override
    public void update(double deltaTime) {
        updateAnimation(deltaTime);
    }

    /**
     * Mở rương, sinh item.
     * @param player ngườ chơi đang tương tác
     * @return true nếu mở thành công
     */
    public boolean open(Player player) {
        if (opened) return false;
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
    public Item getLastLoot() { return lastLoot; }
    public String getRarity() { return rarity; }
    public void setLootTable(LootTable table) { this.lootTable = table; }
}
