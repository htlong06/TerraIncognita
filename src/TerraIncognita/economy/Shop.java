package TerraIncognita.economy;

import TerraIncognita.entity.Player;
import TerraIncognita.item.Item;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Cửa hàng — quản lý mua/bán item với player.
 * prices: map từ Item → giá mua (player trả cho shop).
 * Giá bán (shop trả player) = item.getSellPrice().
 */
public class Shop {

    private final List<Item> items;
    private final Map<Item, Integer> prices;

    public Shop(List<Item> items, Map<Item, Integer> prices) {
        this.items = items;
        this.prices = prices;
    }

    /**
     * Player mua item tại index từ shop.
     * @return true nếu mua thành công
     */
    public boolean buyItem(Player player, int index) {
        if (index < 0 || index >= items.size()) return false;
        Item item = items.get(index);
        int price = prices.getOrDefault(item, 0);
        if (player.getGold() < price) return false;
        if (player.getInventory().isFull()) return false;
        if (!player.spendGold(price)) return false;
        if (!player.getInventory().addItem(item)) {
            // Rollback: hoàn tiền nếu thêm thất bại
            player.addGold(price);
            return false;
        }
        return true;
    }

    /**
     * Player bán item tại index từ inventory cho shop.
     * @return true nếu bán thành công
     */
    public boolean sellItem(Player player, int index) {
        List<Item> invItems = player.getInventory().getItems();
        if (index < 0 || index >= invItems.size()) return false;
        Item item = invItems.get(index);
        int price = item.getSellPrice();
        if (!player.getInventory().removeItem(item)) return false;
        player.addGold(price);
        return true;
    }

    // --- Getter ---
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Map<Item, Integer> getPrices() {
        return Collections.unmodifiableMap(prices);
    }

    public int getPrice(Item item) {
        return prices.getOrDefault(item, 0);
    }
}
