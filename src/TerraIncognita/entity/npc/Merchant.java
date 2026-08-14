package TerraIncognita.entity.npc;

import TerraIncognita.economy.Shop;
import TerraIncognita.entity.Player;
import TerraIncognita.item.BombItem;
import TerraIncognita.item.Item;
import TerraIncognita.item.Potion;
import TerraIncognita.util.Constants;
import java.awt.Rectangle;
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

        BombItem bomb = new BombItem("bomb_shop", "Bomb");
        bomb.setBuyPrice(30);
        bomb.setSellPrice(10);

        // Bình xanh biển — hồi máu dần (regen) trong 1 khoảng thời gian
        Potion regenPotion = Potion.createRegen("regen_shop", "Regen Potion", 5, 10.0);
        regenPotion.setBuyPrice(60);
        regenPotion.setSellPrice(30);

        // Bình xanh lá — cộng thẳng EXP
        Potion expPotion = Potion.createExp("exp_shop", "Exp Potion", 20);
        expPotion.setBuyPrice(80);
        expPotion.setSellPrice(40);

        List<Item> shopItems = List.of(hpPotion, bomb, regenPotion, expPotion);
        Map<Item, Integer> prices = new HashMap<>();
        prices.put(hpPotion, 50);
        prices.put(bomb, 30);
        prices.put(regenPotion, 60);
        prices.put(expPotion, 80);

        this.shop = new Shop(shopItems, prices);
    }

    @Override
    public void interact(Player player) {
        // GameEngine sẽ wire state SHOP ở Task 6.
        // Hiện tại chỉ stub — caller kiểm tra getShop() để mở UI.
    }

    /**
     * Vùng tương tác của merchant — hình chữ nhật mở rộng 1 ô TILE_SIZE
     * ra mỗi phía so với vị trí world. Player đứng trong vùng này thì
     * có thể mở shop (nhấn F). Merchant có ưu tiên cao hơn chest.
     * @return Rectangle bao phủ vùng tương tác
     */
    @Override
    public Rectangle getInteractionBounds() {
        int ts = Constants.TILE_SIZE;
        int x = (int) Math.round(worldX) - ts;
        int y = (int) Math.round(worldY) - ts;
        int w = ts * 3;
        int h = ts * 3;
        return new Rectangle(x, y, w, h);
    }

    public Shop getShop() {
        return shop;
    }
}
