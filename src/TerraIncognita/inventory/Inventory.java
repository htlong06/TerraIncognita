package TerraIncognita.inventory;

import TerraIncognita.entity.Player;
import TerraIncognita.item.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quản lý túi đồ (inventory) của player.
 * Giới hạn số slot, hỗ trợ xếp chồng item stackable.
 */
public class Inventory {

    private List<Item> items;
    private int maxSlots;

    public Inventory(int maxSlots) {
        this.items = new ArrayList<>();
        this.maxSlots = maxSlots;
    }

    /**
     * Thêm item vào túi.
     * Nếu item stackable và đã có item cùng loại → tăng stackCount.
     * @param item item cần thêm
     * @return true nếu thêm thành công (còn slot)
     */
    public boolean addItem(Item item) {
        // Thử stack nếu stackable
        if (item.isStackable()) {
            for (Item existing : items) {
                if (existing.getId().equals(item.getId()) &&
                    existing.getStackCount() < existing.getMaxStack()) {
                    int canAdd = existing.getMaxStack() - existing.getStackCount();
                    int toAdd = Math.min(canAdd, item.getStackCount());
                    existing.setStackCount(existing.getStackCount() + toAdd);
                    item.setStackCount(item.getStackCount() - toAdd);
                    if (item.getStackCount() <= 0) return true;
                }
            }
        }

        // Thêm vào slot mới
        if (items.size() < maxSlots) {
            items.add(item);
            return true;
        }
        return false;
    }

    /**
     * Xoá item khỏi túi.
     * @param item item cần xoá
     * @return true nếu xoá thành công
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    /**
     * Tìm item theo ID.
     */
    public Item findById(String itemId) {
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Kiểm tra có item với ID cụ thể không.
     */
    public boolean hasItem(String itemId) {
        return findById(itemId) != null;
    }

    /**
     * Kiểm tra túi đầy chưa.
     */
    public boolean isFull() {
        return items.size() >= maxSlots;
    }

    /**
     * Lấy danh sách item (read-only).
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Lấy số slot đang dùng.
     */
    public int getUsedSlots() {
        return items.size();
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    /**
     * Sử dụng item tại vị trí index.
     * @param index vị trí item trong túi
     * @param player người chơi sử dụng item
     * @return true nếu sử dụng thành công
     */
    public boolean useItem(int index, Player player) {
        if (index < 0 || index >= items.size()) return false;
        Item item = items.get(index);
        boolean used = item.use(player);
        if (used && item.isStackable() && item.getStackCount() <= 0) {
            items.remove(index);
        }
        return used;
    }
}
