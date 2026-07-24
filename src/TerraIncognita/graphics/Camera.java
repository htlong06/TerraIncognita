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

    private int offsetX, offsetY;          // offset hiện tại (pixel)
    private int screenWidth, screenHeight;  // kích thước cửa sổ game
    private int mapWidthPixels, mapHeightPixels; // kích thước map tính bằng pixel

    /**
     * @param screenWidth chiều rộng cửa sổ (pixel)
     * @param screenHeight chiều cao cửa sổ (pixel)
     */
    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.offsetX = 0;
        this.offsetY = 0;
        // Mặc định bằng kích thước màn hình cho tới khi setMapSize được gọi,
        // để clamp không gây lỗi (maxOffset = 0) trước khi có map thật.
        this.mapWidthPixels = screenWidth;
        this.mapHeightPixels = screenHeight;
    }

    /**
     * Cập nhật offset để camera căn giữa theo vị trí target (player).
     * @param targetX vị trí X của player (pixel) — nên là tâm sprite, không phải góc trên-trái
     * @param targetY vị trí Y của player (pixel)
     */
    public void update(int targetX, int targetY) {
        offsetX = targetX - screenWidth / 2;
        offsetY = targetY - screenHeight / 2;

        // Clamp offset để không vượt ra ngoài map.
        // Nếu map nhỏ hơn màn hình theo 1 chiều nào đó, căn giữa map theo chiều đó
        // (maxOffset < 0 => offset cố định ở giữa) thay vì kẹp cứng về 0.
        int maxOffsetX = mapWidthPixels - screenWidth;
        int maxOffsetY = mapHeightPixels - screenHeight;

        if (maxOffsetX <= 0) {
            offsetX = maxOffsetX / 2;
        } else {
            offsetX = clamp(offsetX, 0, maxOffsetX);
        }

        if (maxOffsetY <= 0) {
            offsetY = maxOffsetY / 2;
        } else {
            offsetY = clamp(offsetY, 0, maxOffsetY);
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Đặt kích thước map (để clamp camera).
     */
    public void setMapSize(int mapWidthPixels, int mapHeightPixels) {
        this.mapWidthPixels = mapWidthPixels;
        this.mapHeightPixels = mapHeightPixels;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Chuyển toạ độ màn hình (VD: vị trí chuột) sang toạ độ thế giới (world).
     * Cần dùng bất cứ khi nào so sánh toạ độ chuột với vị trí world của entity
     * (ví dụ: ngắm bắn cung), vì camera có thể đã cuộn màn hình.
     */
    public int screenToWorldX(int screenX) {
        return screenX + offsetX;
    }

    public int screenToWorldY(int screenY) {
        return screenY + offsetY;
    }

    /**
     * Chuyển toạ độ world sang toạ độ màn hình (ngược lại với trên).
     */
    public int worldToScreenX(int worldX) {
        return worldX - offsetX;
    }

    public int worldToScreenY(int worldY) {
        return worldY - offsetY;
    }
}
