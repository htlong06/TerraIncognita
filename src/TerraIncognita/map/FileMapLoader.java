package TerraIncognita.map;

import TerraIncognita.entity.monster.SlimeMonster;
import TerraIncognita.util.Constants;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
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

/**
 * Load bản đồ từ file text (.txt).
 */
public class FileMapLoader implements MapGenerator {

    private String filePath;

    public FileMapLoader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public GameMap generate(int width, int height, int difficulty) {
        ArrayList<String> lines = new ArrayList<>();
        int mapWidth = 0;
        int mapHeight = 0;

        // Đọc dữ liệu thô từ file text hầm ngục
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                lines.add(line);
                mapWidth = Math.max(mapWidth, line.length());
            }
            mapHeight = lines.size();
        } catch (IOException e) {
            System.err.println("Lỗi đọc file map: " + e.getMessage());
            return null;
        }

        // Khởi tạo đối tượng GameMap dựa trên kích thước thực tế của file text
        GameMap gameMap = new GameMap(mapWidth, mapHeight);

        // Duyệt qua từng ký tự để phân tách ô đất và thực thể
        for (int y = 0; y < mapHeight; y++) {
            String line = lines.get(y);
            for (int x = 0; x < mapWidth; x++) {
                if (x >= line.length()) {
                    gameMap.setTile(x, y, new Tile(TileType.VOID));
                    continue;
                }
                char c = line.charAt(x);
                parseTileAndEntities(gameMap, c, x, y);
            }
        }
        return gameMap;
    }

    private void parseTileAndEntities(GameMap map, char c, int x, int y) {
        switch (c) {
            case '#':
                map.setTile(x, y, new Tile(TileType.WALL));
                break;
            case '.':
                map.setTile(x, y, new Tile(TileType.FLOOR));
                break;
            case 'D':
                map.setTile(x, y, new Tile(TileType.DOOR));
                break;
            case 'T':
                map.setTile(x, y, new Tile(TileType.TRAP));
                break;
            case 'K':
                map.setTile(x, y, new Tile(TileType.CHECKPOINT));
                break;
            case 'S':
                map.setTile(x, y, new Tile(TileType.STAIR_DOWN));
                map.setStairs(x, y);
                break;
            case 'P':
                map.setTile(x, y, new Tile(TileType.FLOOR));
                map.setPlayerStart(x, y); // Lưu vị trí xuất phát của Player
                break;
            case 'M':
                map.setTile(x, y, new Tile(TileType.FLOOR));
                // Tự động tạo quái vật SlimeMonster tại vị trí ô lưới (x, y)
                SlimeMonster slime = new SlimeMonster(x, y);
                map.addEntity(slime);
                break;
            case 'C':
                map.setTile(x, y, new Tile(TileType.CHEST_TILE));
                break;
            default:
                map.setTile(x, y, new Tile(TileType.VOID));
                break;
        }
    }
}