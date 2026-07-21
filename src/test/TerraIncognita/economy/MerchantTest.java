package TerraIncognita.economy;

import TerraIncognita.entity.Player;
import TerraIncognita.entity.npc.Merchant;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.Potion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Shop + Merchant buy/sell logic.
 */
class MerchantTest {

    private Player player;
    private Shop shop;

    @BeforeEach
    void setUp() {
        player = new Player();

        // Tạo shop test: 1 potion (giá 50) + 1 sword (giá 120)
        Potion potion = new Potion("hp_test", "Health Potion", 30);
        potion.setBuyPrice(50);
        potion.setSellPrice(25);

        Equipment sword = new Equipment("sword_test", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        sword.setBuyPrice(120);
        sword.setSellPrice(60);

        Map<Item, Integer> prices = new HashMap<>();
        prices.put(potion, 50);
        prices.put(sword, 120);

        shop = new Shop(List.of(potion, sword), prices);
    }

    @Test
    void buyItem_enoughGold_buysSuccessfully() {
        player.addGold(100);

        boolean result = shop.buyItem(player, 0); // mua potion giá 50

        assertTrue(result);
        assertEquals(50, player.getGold());
        assertEquals(1, player.getInventory().getUsedSlots());
    }

    @Test
    void buyItem_notEnoughGold_returnsFalse() {
        player.addGold(30);

        boolean result = shop.buyItem(player, 0); // potion giá 50, chỉ có 30

        assertFalse(result);
        assertEquals(30, player.getGold());
        assertEquals(0, player.getInventory().getUsedSlots());
    }

    @Test
    void buyItem_inventoryFull_returnsFalse() {
        player.addGold(1000);
        // Lấp đầy inventory — max slots = Constants.INVENTORY_MAX_SLOTS
        for (int i = 0; i < player.getInventory().getMaxSlots(); i++) {
            player.getInventory().addItem(new Potion("fill_" + i, "Fill", 1));
        }

        boolean result = shop.buyItem(player, 0);

        assertFalse(result);
        // Gold không đổi
        assertEquals(1000, player.getGold());
    }

    @Test
    void sellItem_removesItem_andAddsGold() {
        player.getInventory().addItem(new Potion("sell_test", "Health Potion", 30));
        // Set sellPrice = 25 cho item vừa thêm
        player.getInventory().getItems().get(0).setSellPrice(25);

        boolean result = shop.sellItem(player, 0);

        assertTrue(result);
        assertEquals(25, player.getGold());
        assertEquals(0, player.getInventory().getUsedSlots());
    }

    @Test
    void merchant_initShop_hasItems() {
        Merchant merchant = new Merchant(5, 5);

        Shop mShop = merchant.getShop();
        assertNotNull(mShop);
        assertTrue(mShop.getItems().size() >= 2);
    }
}
