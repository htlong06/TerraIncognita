package TerraIncognita.economy;

import TerraIncognita.item.Item;
import java.util.List;
import java.util.Random;

/**
 * Bảng loot — xác định item có thể rơi khi mở rương / đánh quái.
 * Có dropChance (0.0-1.0); nếu roll thành công thì chọn 1 item ngẫu nhiên.
 */
public class LootTable {

    private final List<Item> possibleItems;
    private final double dropChance;
    private final Random random;

    public LootTable(List<Item> items, double dropChance) {
        this(items, dropChance, new Random());
    }

    public LootTable(List<Item> items, double dropChance, Random random) {
        this.possibleItems = items;
        this.dropChance = dropChance;
        this.random = random;
    }

    /**
     * Sinh loot: roll dropChance, nếu thành công trả về 1 item ngẫu nhiên.
     * @return item ngẫu nhiên, hoặc null nếu roll thất bại / list rỗng
     */
    public Item generateLoot() {
        if (possibleItems == null || possibleItems.isEmpty()) {
            return null;
        }
        if (random.nextDouble() >= dropChance) {
            return null;
        }
        int index = random.nextInt(possibleItems.size());
        return possibleItems.get(index);
    }

    public List<Item> getPossibleItems() {
        return possibleItems;
    }

    public double getDropChance() {
        return dropChance;
    }
}
