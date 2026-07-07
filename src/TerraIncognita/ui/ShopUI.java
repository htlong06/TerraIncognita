package TerraIncognita.ui;

import java.awt.Graphics2D;

/**
 * Giao diện cửa hàng — mua/bán item với NPC Merchant.
 */
public class ShopUI {

    // TODO: Khai báo các trường
    // - Merchant currentMerchant
    // - int selectedIndex
    // - boolean isOpen

    public void open(/* Merchant merchant */) {
        // TODO: isOpen = true, load danh sách item bán
    }

    public void close() {
        // TODO: isOpen = false
    }

    public void update(double deltaTime /*, InputHandler input */) {
        // TODO: Di chuyển cursor, xác nhận mua/bán
    }

    public void render(Graphics2D g2d) {
        // TODO: Vẽ danh sách item + giá
        // TODO: Vẽ vàng hiện có
        // TODO: Highlight item đang chọn
    }

    public boolean isOpen() {
        // TODO
        return false;
    }
}
