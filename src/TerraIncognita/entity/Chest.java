package TerraIncognita.entity;

/**
 * Rương báu / kho báu.
 * Là Entity đứng yên, khi player tương tác sẽ mở ra và sinh item ngẫu nhiên.
 */
public class Chest extends Entity {

    private boolean opened;
    private boolean locked;
    private String requiredKeyId;
    private String lootTableId;

    public Chest(int tileX, int tileY, boolean locked) {
        super();
        this.name = "Chest";
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldX = tileX * 32;   // TODO: dùng Constants.TILE_SIZE
        this.worldY = tileY * 32;
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
        // TODO (GĐ5): Sinh item vào player inventory
        return true;
    }

    // --- Getter ---
    public boolean isOpened() { return opened; }
    public boolean isLocked() { return locked; }
    public void setRequiredKeyId(String keyId) { this.requiredKeyId = keyId; }
}
