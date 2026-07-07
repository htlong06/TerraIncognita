package TerraIncognita.ui;

import java.awt.Graphics2D;

/**
 * HUD (Heads-Up Display) — thanh HP, thông tin trạng thái hiện trên màn hình.
 * Vẽ ở vị trí cố định, không bị ảnh hưởng bởi Camera.
 */
public class HUD {

    // TODO: Khai báo các trường
    // - int barWidth, barHeight    — kích thước thanh HP
    // - int padding                — khoảng cách từ mép màn hình

    /**
     * Vẽ HUD lên màn hình.
     * @param g2d đối tượng đồ họa
     */
    public void render(Graphics2D g2d /*, Player player */) {
        // TODO: Vẽ thanh HP (nền xám + filled đỏ theo tỉ lệ HP hiện tại / HP tối đa)
        // TODO: Vẽ text: Level, EXP, Gold
        // TODO: Vẽ thông báo tạm thời (damage taken, level up...)
    }
}
