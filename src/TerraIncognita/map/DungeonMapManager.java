package TerraIncognita.map;

import TerraIncognita.entity.Player;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Đóng gói phần "bản đồ hầm ngục": tải map từ file text (xem FileMapLoader
 * để biết bảng ký hiệu — hiện chỉ có nền/tường/cửa), đồng bộ vị trí spawn
 * của player, và vẽ các tile bằng sprite thật từ tileset (fallback màu đặc
 * nếu thiếu asset).
 *
 * File này tách riêng khỏi GameEngine.java để không đụng vào cùng những dòng
 * code mà người khác trong nhóm đang sửa — tránh conflict khi merge.
 *
 * ================== CÁCH TÍCH HỢP VÀO GameEngine.java ==================
 *
 * 1) Thêm field:
 *      private DungeonMapManager mapManager;
 *
 * 2) Trong constructor, SAU khi tạo `this.player = new Player();`:
 *      this.mapManager = new DungeonMapManager("dungeon_1");
 *      this.mapManager.placePlayer(this.player);
 *
 * 3) Trong renderPlaying(), THAY đoạn vẽ tile hiện tại bằng:
 *      mapManager.renderTiles(g2d, assetLoader);
 *
 * Không cần đụng field/method nào khác trong GameEngine.java.
 * ==========================================================================================
 */
public class DungeonMapManager {

    private GameMap currentMap;

    /**
     * @param mapName tên map không kèm đuôi file, VD "dungeon_1" (xem
     *                 FileMapLoader để biết quy ước đặt tên file)
     */
    public DungeonMapManager(String mapName) {
        FileMapLoader mapLoader = new FileMapLoader(mapName);
        this.currentMap = mapLoader.generate(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, 1);
    }

    /** Map hiện tại đang được tải, null nếu file lỗi. */
    public GameMap getCurrentMap() {
        return currentMap;
    }

    public boolean isLoaded() {
        return currentMap != null;
    }

    /**
     * Đặt player vào đúng ô 'P' được khai báo trong file map, và gắn map
     * hiện tại vào player để kích hoạt chống đi xuyên tường.
     * Nếu map lỗi, fallback về giữa màn hình.
     */
    public void placePlayer(Player player) {
        if (currentMap != null) {
            int startX = currentMap.getPlayerStartX();
            int startY = currentMap.getPlayerStartY();
            player.setWorldX(startX * Constants.TILE_SIZE);
            player.setWorldY(startY * Constants.TILE_SIZE);
            player.updateTilePosition(Constants.TILE_SIZE);
            player.setCurrentMap(currentMap);
        } else {
            player.setWorldX(Constants.SCREEN_WIDTH / 2.0 - Constants.TILE_SIZE / 2.0);
            player.setWorldY(Constants.SCREEN_HEIGHT / 2.0 - Constants.TILE_SIZE / 2.0);
        }
    }

    /**
     * Vẽ toàn bộ lớp nền của map bằng sprite thật lấy từ assetLoader theo
     * TileType.getSpriteName() — nếu thiếu sprite, fallback vẽ màu đặc để
     * game vẫn chạy được trong lúc chưa có đủ asset.
     */
    public void renderTiles(Graphics2D g2d, AssetLoader assetLoader) {
        if (currentMap == null) return;

        int tileSize = Constants.TILE_SIZE;
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                Tile tile = currentMap.getTile(x, y);
                drawTile(g2d, tile, x * tileSize, y * tileSize, tileSize, assetLoader);
            }
        }
    }

    private void drawTile(Graphics2D g2d, Tile tile, int px, int py, int size, AssetLoader assetLoader) {
        BufferedImage sprite = assetLoader.getTile(tile.getType().getSpriteName());
        if (sprite != null) {
            g2d.drawImage(sprite, px, py, size, size, null);
            return;
        }

        // Fallback màu đặc khi chưa có sprite cho loại tile này
        switch (tile.getType()) {
            case WALL:
                g2d.setColor(new Color(60, 60, 70));
                g2d.fillRect(px, py, size, size);
                g2d.setColor(new Color(40, 40, 45));
                g2d.drawRect(px, py, size, size);
                break;
            case DOOR:
                g2d.setColor(new Color(139, 69, 19));
                g2d.fillRect(px + 4, py, size - 8, size);
                break;
            default:
                // FLOOR và mọi loại khác chưa dùng tới lúc này
                g2d.setColor(new Color(45, 42, 38));
                g2d.fillRect(px, py, size, size);
                break;
        }
    }
}