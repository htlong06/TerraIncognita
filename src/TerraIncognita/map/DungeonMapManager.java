package TerraIncognita.map;

import TerraIncognita.entity.Player;
import TerraIncognita.util.Constants;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Đóng gói phần "bản đồ hầm ngục": tải map từ file .tmx (Tiled) qua
 * TmxMapLoader, đồng bộ vị trí spawn của player, và vẽ tất cả layer (ground,
 * wall, props, props 2...) chồng lên nhau theo đúng thứ tự gốc trong .tmx,
 * dùng ảnh mà TmxMapLoader đã tự cắt sẵn từ các tileset .tsx.
 *
 * Va chạm (đi được / không) đã được TmxMapLoader tính sẵn vào GameMap dựa
 * theo layer "wall" — DungeonMapManager không cần biết gì thêm về việc đó.
 *
 * Vị trí bắt đầu player KHÔNG lấy từ file .tmx (map hiện chưa khai báo) —
 * placePlayer() vẫn gọi được nhưng sẽ dùng vị trí mặc định (1,1) của GameMap
 * cho tới khi code đặt player ở chỗ khác ghi đè.
 *
 * ================== CÁCH TÍCH HỢP VÀO GameEngine.java ==================
 * 1) Field (không đổi):      private DungeonMapManager mapManager;
 * 2) Constructor (không đổi): mapManager = new DungeonMapManager("dungeon_1");
 * 3) renderPlaying():        mapManager.renderTiles(g2d);   // không có assetLoader
 * ==========================================================================================
 */
public class DungeonMapManager {

    private GameMap currentMap;
    private List<TmxMapLoader.VisualLayer> visualLayers;

    public DungeonMapManager(String mapName) {
        TmxMapLoader mapLoader = new TmxMapLoader(mapName);
        this.currentMap = mapLoader.generate(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, 1);
        this.visualLayers = mapLoader.getVisualLayers();
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public boolean isLoaded() {
        return currentMap != null;
    }

    /**
     * Đặt player theo GameMap.getPlayerStart{X,Y}() (mặc định (1,1) nếu
     * chưa có ai set khác), và gắn map hiện tại vào player để chống xuyên
     * tường. Nếu map lỗi hoàn toàn, fallback về giữa màn hình.
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
     * Vẽ toàn bộ layer (ground, wall, props, props 2...) chồng lên nhau
     * theo đúng thứ tự xuất hiện trong file .tmx gốc.
     */
    public void renderTiles(Graphics2D g2d) {
        if (currentMap == null) return;

        int tileSize = Constants.TILE_SIZE;
        int mapWidth = currentMap.getWidth();
        int mapHeight = currentMap.getHeight();

        for (TmxMapLoader.VisualLayer layer : visualLayers) {
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    BufferedImage img = layer.images[y][x];
                    if (img == null) continue;
                    int px = x * tileSize + layer.offsetXPx;
                    int py = y * tileSize + layer.offsetYPx;
                    g2d.drawImage(img, px, py, tileSize, tileSize, null);
                }
            }
        }
    }
}