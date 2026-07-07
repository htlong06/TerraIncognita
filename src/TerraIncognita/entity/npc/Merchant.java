package TerraIncognita.entity.npc;

import TerraIncognita.entity.Player;

/**
 * NPC Thương nhân — mua/bán vật phẩm.
 * Khi tương tác: mở giao diện Shop (ShopUI).
 */
public class Merchant extends NPC {

    // TODO: Khai báo các trường
    // - List<Item> shopInventory    — danh sách item đang bán
    // - Map<Item, Integer> prices   — giá mỗi item

    public Merchant(int tileX, int tileY) {
        super("Merchant", tileX, tileY);
        // TODO: Khởi tạo shop inventory
    }

    @Override
    public void interact(Player player) {
        // TODO: Mở giao diện Shop
        // TODO: GameEngine chuyển state sang SHOP
    }

    // TODO: Phương thức mua/bán
    // public boolean buyItem(Player player, Item item) { ... }
    // public boolean sellItem(Player player, Item item) { ... }
}
