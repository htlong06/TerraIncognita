package TerraIncognita.graphics;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Load và cache toàn bộ tài nguyên ảnh (sprites, tiles, UI).
 * Chỉ load 1 lần khi khởi động game, lưu vào Map để tái sử dụng.
 *
 * Cách dùng:
 *   AssetLoader assets = new AssetLoader();
 *   assets.loadAll();
 *   BufferedImage[] playerWalk = assets.getFrames("player_walk");
 *   BufferedImage wallTile = assets.getTile("wall");
 */
public class AssetLoader {

    // TODO: Khai báo các trường
    // - Map<String, BufferedImage[]> spriteFrames   — animation frames theo tên
    // - Map<String, BufferedImage> tileImages        — ảnh tile đơn lẻ
    // - Map<String, BufferedImage> uiImages          — ảnh giao diện

    /**
     * Load toàn bộ tài nguyên ảnh từ thư mục resources/sprites/.
     * Gọi 1 lần duy nhất khi khởi động game.
     */
    public void loadAll() {
        // TODO: Load tile images từ resources/sprites/tiles/
        // TODO: Load player frames từ resources/sprites/player/
        // TODO: Load monster frames từ resources/sprites/monsters/
        // TODO: Load NPC frames từ resources/sprites/npc/
        // TODO: Load item images từ resources/sprites/items/
        // TODO: Load UI images từ resources/sprites/ui/
    }

    /**
     * Lấy mảng frame animation theo tên.
     * @param name tên sprite (ví dụ: "player_walk_down", "slime_idle")
     * @return mảng BufferedImage[] các frame
     */
    public BufferedImage[] getFrames(String name) {
        // TODO: return spriteFrames.get(name)
        return null;
    }

    /**
     * Lấy ảnh tile theo tên.
     * @param name tên tile (ví dụ: "wall", "floor", "door")
     * @return BufferedImage ảnh tile
     */
    public BufferedImage getTile(String name) {
        // TODO: return tileImages.get(name)
        return null;
    }

    /**
     * Lấy ảnh UI theo tên.
     */
    public BufferedImage getUI(String name) {
        // TODO: return uiImages.get(name)
        return null;
    }

    // TODO: Phương thức helper
    // private BufferedImage loadImage(String path) { ... }      — đọc 1 file ảnh bằng ImageIO.read()
    // private BufferedImage[] loadFrames(String basePath, int count) { ... } — load nhiều frame
}
