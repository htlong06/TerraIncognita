package TerraIncognita.graphics;

import java.awt.Graphics2D;

/**
 * Vẽ mọi thứ lên Graphics2D: map, entity, HUD.
 *
 * Entity không tự vẽ chính mình — Renderer lấy dữ liệu từ Entity
 * (vị trí, animation hiện tại) rồi gọi drawImage.
 * Điều này giúp tách rời logic game khỏi phần hiển thị.
 *
 * Trách nhiệm:
 * - Vẽ các tile map (có tính Camera offset)
 * - Vẽ entity (Player, Monster, NPC, Chest...) theo animation hiện tại
 * - Vẽ HUD (thanh HP, thông tin, inventory overlay...)
 * - Vẽ các màn hình UI (menu, game over, dialog...)
 */
public class Renderer {

    // TODO: Khai báo các trường
    // - AssetLoader assetLoader
    // - Camera camera

    public Renderer(AssetLoader assetLoader, Camera camera) {
        // TODO
    }

    /**
     * Vẽ toàn bộ bản đồ (các tile trong tầm nhìn camera).
     * @param g2d đối tượng đồ họa
     * @param map bản đồ hiện tại
     */
    public void renderMap(Graphics2D g2d /*, GameMap map */) {
        // TODO: Duyệt mảng 2D tile, chỉ vẽ tile nằm trong viewport camera
        // TODO: g2d.drawImage(tileImage, x * TILE_SIZE - camera.getOffsetX(), ...)
    }

    /**
     * Vẽ tất cả entity trên map.
     * @param g2d đối tượng đồ họa
     */
    public void renderEntities(Graphics2D g2d /*, List<Entity> entities */) {
        // TODO: Duyệt danh sách entity, vẽ frame animation hiện tại
        // TODO: Vị trí vẽ = entity.position - camera.offset
    }

    /**
     * Vẽ HUD (thanh HP, level, thông tin trạng thái).
     * HUD vẽ ở vị trí cố định trên màn hình (không bị ảnh hưởng bởi camera).
     */
    public void renderHUD(Graphics2D g2d /*, Player player */) {
        // TODO: Vẽ thanh HP (filled rectangle)
        // TODO: Vẽ text level, EXP
        // TODO: Vẽ mini-map (tuỳ chọn)
    }

    /**
     * Vẽ hiệu ứng damage/text nổi lên.
     */
    public void renderEffects(Graphics2D g2d) {
        // TODO: Vẽ damage numbers, level up text...
    }
}
