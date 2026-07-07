package TerraIncognita.map;

/**
 * Load bản đồ từ file text (.txt).
 *
 * Format file mẫu (mỗi ký tự = 1 tile):
 *   # = WALL
 *   . = FLOOR
 *   D = DOOR
 *   S = STAIR_DOWN
 *   U = STAIR_UP
 *   T = TRAP
 *   C = CHEST
 *   K = CHECKPOINT
 *   W = SWITCH
 *   X = SECRET_WALL
 *   P = Player start position (FLOOR)
 *   M = Monster spawn (FLOOR)
 *   N = NPC position (FLOOR)
 */
public class FileMapLoader implements MapGenerator {

    // TODO: Khai báo các trường
    // - String filePath

    public FileMapLoader(String filePath) {
        // TODO: Gán filePath
    }

    @Override
    public GameMap generate(int width, int height, int difficulty) {
        // TODO: Đọc file text, parse từng ký tự thành TileType
        // TODO: Tạo GameMap, set tile tương ứng
        // TODO: Phát hiện ký tự đặc biệt (P, M, N) để đặt entity
        // Tham số width, height có thể bị ignore nếu kích thước lấy từ file
        return null;
    }

    // TODO: Phương thức helper
    // private TileType charToTileType(char c) { ... }
    // private void parseSpecialChars(GameMap map, char c, int x, int y) { ... }
}
