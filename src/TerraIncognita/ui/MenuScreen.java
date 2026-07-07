package TerraIncognita.ui;

import java.awt.Graphics2D;

/**
 * Màn hình Menu chính.
 * Hiện khi khởi động game: New Game, Continue, Settings, Exit.
 */
public class MenuScreen {

    // TODO: Khai báo các trường
    // - String[] menuOptions       — các lựa chọn
    // - int selectedIndex          — lựa chọn đang được highlight
    // - boolean hasSaveFile        — có save file không (để enable/disable Continue)

    public MenuScreen() {
        // TODO
    }

    /**
     * Cập nhật logic menu (nhận input lên/xuống/Enter).
     */
    public void update(double deltaTime /*, InputHandler input */) {
        // TODO: Phím UP/DOWN → thay đổi selectedIndex
        // TODO: Phím ENTER → thực hiện lựa chọn
    }

    /**
     * Vẽ menu lên màn hình.
     */
    public void render(Graphics2D g2d) {
        // TODO: Vẽ tiêu đề game
        // TODO: Vẽ các option, highlight option đang chọn
    }

    /**
     * Lấy lựa chọn đã chọn (sau khi nhấn Enter).
     * @return index lựa chọn, hoặc -1 nếu chưa chọn
     */
    public int getSelectedOption() {
        // TODO
        return -1;
    }
}
