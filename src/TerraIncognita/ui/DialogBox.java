package TerraIncognita.ui;

import java.awt.Graphics2D;

/**
 * Hộp thoại hiện text — dùng cho NPC dialog, thông báo, lore object.
 */
public class DialogBox {

    // TODO: Khai báo các trường
    // - String[] lines          — các dòng text
    // - int currentLine         — dòng đang hiển thị
    // - boolean isActive        — đang hiện không
    // - int boxX, boxY, boxWidth, boxHeight

    /**
     * Hiện hộp thoại với nội dung.
     * @param text nội dung (có thể nhiều dòng, ngăn cách bằng \n)
     */
    public void show(String text) {
        // TODO: Parse text thành lines, isActive = true
    }

    /**
     * Cập nhật: nhấn phím → chuyển dòng tiếp, hết dòng → đóng.
     */
    public void update(double deltaTime /*, InputHandler input */) {
        // TODO
    }

    /**
     * Vẽ hộp thoại.
     */
    public void render(Graphics2D g2d) {
        // TODO: Vẽ nền hộp (rectangle bán trong suốt)
        // TODO: Vẽ text dòng hiện tại
        // TODO: Vẽ indicator "nhấn để tiếp tục"
    }

    public boolean isActive() {
        // TODO
        return false;
    }
}
