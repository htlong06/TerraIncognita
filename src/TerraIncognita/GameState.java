package TerraIncognita;

/**
 * Enum các trạng thái của game.
 * Dùng trong GameEngine để điều khiển luồng chương trình.
 */
public enum GameState {
    MENU,           // Màn hình menu chính
    HUB,            // Khu vực Hub (làng an toàn)
    PLAYING,        // Đang khám phá dungeon
    PAUSED,         // Tạm dừng
    COMBAT,         // Đang trong trận đánh (tuỳ chọn nếu muốn tách riêng)
    INVENTORY,      // Đang mở túi đồ
    SHOP,           // Đang giao dịch với NPC
    DIALOG,         // Đang hiện hộp thoại
    RADIAL_MENU,    // Menu vòng tròn (giữ TAB)
    RUN_SUMMARY,    // Màn hình tổng kết sau lượt chơi
    GAME_OVER       // Thua — hiện kết quả
}
