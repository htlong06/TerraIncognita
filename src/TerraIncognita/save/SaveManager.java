package TerraIncognita.save;

/**
 * Quản lý lưu/tải game.
 * Đọc/ghi trạng thái game ra file JSON tại checkpoint hoặc Hub.
 *
 * Dữ liệu cần lưu:
 * - Player: vị trí, chỉ số (HP, ATK, DEF, Level, EXP, Gold), inventory, equipment
 * - Dungeon progress: tầng hiện tại, map state
 * - Thống kê: RunSummary tích luỹ
 */
public class SaveManager {

    // TODO: Khai báo các trường
    // - String saveDirectory     — thư mục lưu save (resources/saves/)

    public SaveManager(String saveDirectory) {
        // TODO
    }

    /**
     * Lưu game vào file JSON.
     * @param slotName tên slot (ví dụ: "save_1", "auto_save")
     */
    public void saveGame(String slotName /*, Player player, GameEngine engine */) {
        // TODO: Thu thập dữ liệu → tạo JSON string → ghi ra file
        // Gợi ý: dùng StringBuilder để tạo JSON thủ công,
        //         hoặc dùng thư viện đơn giản nếu học phần cho phép
    }

    /**
     * Tải game từ file JSON.
     * @param slotName tên slot
     * @return true nếu tải thành công
     */
    public boolean loadGame(String slotName /*, Player player, GameEngine engine */) {
        // TODO: Đọc file JSON → parse → khôi phục trạng thái
        return false;
    }

    /**
     * Kiểm tra có save file tồn tại không.
     */
    public boolean hasSaveFile(String slotName) {
        // TODO: Kiểm tra file tồn tại
        return false;
    }

    /**
     * Xoá save file.
     */
    public void deleteSave(String slotName) {
        // TODO
    }

    // TODO: Phương thức helper
    // private String toJson(/* dữ liệu */) { ... }
    // private void fromJson(String json /*, Player player */) { ... }
}
