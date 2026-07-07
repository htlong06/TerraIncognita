package TerraIncognita.graphics;

/**
 * Camera / Viewport theo dõi player.
 *
 * Khi bản đồ lớn hơn cửa sổ game, Camera xác định phần nào của map
 * được hiển thị trên màn hình. Camera luôn căn giữa theo vị trí player.
 *
 * Cách dùng:
 *   camera.update(player.getX(), player.getY());
 *   // Khi vẽ: vị trí vẽ = worldPosition - camera.offset
 *   int screenX = entity.x - camera.getOffsetX();
 */
public class Camera {

    // TODO: Khai báo các trường
    // - int offsetX, offsetY        — offset hiện tại (pixel)
    // - int screenWidth, screenHeight — kích thước cửa sổ game
    // - int mapWidthPixels, mapHeightPixels — kích thước map tính bằng pixel

    /**
     * @param screenWidth chiều rộng cửa sổ (pixel)
     * @param screenHeight chiều cao cửa sổ (pixel)
     */
    public Camera(int screenWidth, int screenHeight) {
        // TODO
    }

    /**
     * Cập nhật offset để camera căn giữa theo vị trí target (player).
     * @param targetX vị trí X của player (pixel)
     * @param targetY vị trí Y của player (pixel)
     */
    public void update(int targetX, int targetY) {
        // TODO: offsetX = targetX - screenWidth / 2
        // TODO: offsetY = targetY - screenHeight / 2
        // TODO: Clamp offset để không vượt ra ngoài map
    }

    /**
     * Đặt kích thước map (để clamp camera).
     */
    public void setMapSize(int mapWidthPixels, int mapHeightPixels) {
        // TODO
    }

    public int getOffsetX() {
        // TODO
        return 0;
    }

    public int getOffsetY() {
        // TODO
        return 0;
    }
}
