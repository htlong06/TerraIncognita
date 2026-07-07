package TerraIncognita.ui;

import java.awt.Graphics2D;

/**
 * Giao diện túi đồ (Inventory).
 * Hiện khi player bấm I: danh sách item, có thể sử dụng/trang bị/vứt.
 */
public class InventoryUI {

    // TODO: Khai báo các trường
    // - int selectedSlot       — slot đang chọn
    // - boolean isOpen         — đang mở không

    public void render(Graphics2D g2d /*, Inventory inventory */) {
        // TODO: Vẽ lưới slot
        // TODO: Vẽ icon item trong mỗi slot
        // TODO: Highlight slot đang chọn
        // TODO: Vẽ thông tin item đang chọn (tên, mô tả, hành động)
    }

    public void update(double deltaTime /*, InputHandler input */) {
        // TODO: Di chuyển cursor giữa các slot
        // TODO: Phím Enter → sử dụng/trang bị
        // TODO: Phím Delete → vứt
    }

    public boolean isOpen() {
        // TODO
        return false;
    }

    public void toggle() {
        // TODO: isOpen = !isOpen
    }
}
