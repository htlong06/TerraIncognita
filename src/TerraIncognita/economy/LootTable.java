package TerraIncognita.economy;

import TerraIncognita.item.Item;
import java.util.List;

/**
 * Bảng loot — xác định item có thể rơi khi mở rương / đánh quái.
 * Có dropChance (0.0-1.0); nếu roll thành công thì chọn 1 item ngẫu nhiên.
 */
public class LootTable {

    private final List<Item> possibleItems;
    private final double dropChance;

    public LootTable(List<Item> items, double dropChance) {
        this.possibleItems = items;
        this.dropChance = dropChance;
    }

    /**
     * Sinh loot: roll dropChance, nếu thành công trả về 1 item ngẫu nhiên.
     * @return item ngẫu nhiên, hoặc null nếu roll thất bại / list rỗng
     */
    public Item generateLoot() {
        if (possibleItems == null || possibleItems.isEmpty()) {
            return null;
        }
        if (Math.random() >= dropChance) {
            return null;
        }
        int index = (int) (Math.random() * possibleItems.size());
        return possibleItems.get(index);
    }

    public List<Item> getPossibleItems() {
        return possibleItems;
    }

    public double getDropChance() {
        return dropChance;
    }
}
