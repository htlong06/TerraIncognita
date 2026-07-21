package TerraIncognita.map;

import TerraIncognita.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Load bản đồ từ 1 file text (.txt) — hiện tại chỉ hỗ trợ nền/tường/cửa.
 *
 * ================= BẢNG KÝ HIỆU =================
 *   #  = WALL
 *   .  = FLOOR
 *   D  = DOOR
 *   P  = vị trí bắt đầu player (nền là FLOOR, đúng 1 ký tự mỗi map)
 *
 * Kích thước map lấy từ chính nội dung file (số dòng, độ dài dòng dài nhất).
 * Cần thêm loại tile khác (bẫy, cầu thang, rương...) sau này thì thêm case
 * mới vào charToTileType(), không cần đổi cấu trúc file hay vòng lặp đọc.
 */
public class FileMapLoader implements MapGenerator {

    private final String filePath;

    /**
     * @param mapName tên map không kèm đuôi file, VD "dungeon_1" — loader sẽ
     *                 tự tìm "dungeon_1.txt" trong thư mục Constants.MAPS_PATH.
     */
    public FileMapLoader(String mapName) {
        this.filePath = Constants.MAPS_PATH + mapName + ".txt";
    }

    @Override
    public GameMap generate(int width, int height, int difficulty) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            System.err.println("[FileMapLoader] Không đọc được file map: " + filePath
                    + " (" + e.getMessage() + ")");
            return null;
        }

        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        if (lines.isEmpty()) {
            System.err.println("[FileMapLoader] File map rỗng: " + filePath);
            return null;
        }

        int mapHeight = lines.size();
        int mapWidth = 0;
        for (String line : lines) {
            mapWidth = Math.max(mapWidth, line.length());
        }

        GameMap map = new GameMap(mapWidth, mapHeight);
        boolean playerFound = false;

        for (int y = 0; y < mapHeight; y++) {
            String line = lines.get(y);
            for (int x = 0; x < mapWidth; x++) {
                char c = (x < line.length()) ? line.charAt(x) : '#';

                map.setTile(x, y, new Tile(charToTileType(c)));

                if (c == 'P') {
                    map.setPlayerStart(x, y);
                    playerFound = true;
                }
            }
        }

        if (!playerFound) {
            System.err.println("[FileMapLoader] Cảnh báo: không tìm thấy ký tự 'P' trong "
                    + filePath + " — player sẽ dùng vị trí mặc định của GameMap.");
        }

        return map;
    }

    /** Chuyển 1 ký tự trong file thành TileType tương ứng. */
    private TileType charToTileType(char c) {
        switch (c) {
            case '#': return TileType.WALL;
            case '.': return TileType.FLOOR;
            case 'D': return TileType.DOOR;
            case 'P': return TileType.FLOOR; // player đứng trên nền FLOOR
            default:
                System.err.println("[FileMapLoader] Ký tự không xác định '" + c + "' -> mặc định WALL");
                return TileType.WALL;
        }
    }
}