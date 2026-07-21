package TerraIncognita.entity.npc;

import TerraIncognita.economy.Shop;
import TerraIncognita.entity.Player;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.Potion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NPC Thương nhân — mua/bán vật phẩm.
 * Khi tương tác: mở giao diện Shop (ShopUI).
 */
public class Merchant extends NPC {

    private Shop shop;

    public Merchant(int tileX, int tileY) {
        super("Merchant", tileX, tileY);
        initShop();
    }

    /**
     * Khởi tạo shop inventory — hardcode các item bán.
     */
    private void initShop() {
        // Tạo item mới cho mỗi lần mua (tránh share reference)
        Potion hpPotion = new Potion("hp_shop", "Health Potion", 30);
        hpPotion.setBuyPrice(50);
        hpPotion.setSellPrice(25);

        Equipment ironSword = new Equipment("sword_shop", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        ironSword.setBuyPrice(120);
        ironSword.setSellPrice(60);

        List<Item> shopItems = List.of(hpPotion, ironSword);
        Map<Item, Integer> prices = new HashMap<>();
        prices.put(hpPotion, 50);
        prices.put(ironSword, 120);

        this.shop = new Shop(shopItems, prices);
    }

    @Override
    public void interact(Player player) {
        // GameEngine sẽ wire state SHOP ở Task 6.
        // Hiện tại chỉ stub — caller kiểm tra getShop() để mở UI.
    }

    public Shop getShop() {
        return shop;
    }
}
