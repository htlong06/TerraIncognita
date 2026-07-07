package TerraIncognita.graphics;

import java.awt.image.BufferedImage;

/**
 * Cắt sprite sheet (1 ảnh lớn chứa nhiều frame) thành mảng BufferedImage[].
 *
 * Sprite sheet là cách tổ chức phổ biến: gộp nhiều frame animation vào 1 file PNG,
 * mỗi frame có kích thước cố định (ví dụ 32x32), xếp theo hàng/cột.
 *
 * Cách dùng:
 *   SpriteSheet sheet = new SpriteSheet(ImageIO.read(...), 32, 32);
 *   BufferedImage frame = sheet.getFrame(col, row);
 *   BufferedImage[] walkFrames = sheet.getRow(0, 4); // 4 frame ở hàng 0
 */
public class SpriteSheet {

    // TODO: Khai báo các trường
    // - BufferedImage sheet      — ảnh gốc (sprite sheet)
    // - int frameWidth           — chiều rộng 1 frame (pixel)
    // - int frameHeight          — chiều cao 1 frame (pixel)

    /**
     * @param sheet ảnh sprite sheet đã load
     * @param frameWidth chiều rộng mỗi frame (pixel)
     * @param frameHeight chiều cao mỗi frame (pixel)
     */
    public SpriteSheet(BufferedImage sheet, int frameWidth, int frameHeight) {
        // TODO: Gán các trường
    }

    /**
     * Lấy 1 frame tại vị trí (col, row) trên sprite sheet.
     * @param col cột (0-indexed)
     * @param row hàng (0-indexed)
     * @return BufferedImage frame cắt ra
     */
    public BufferedImage getFrame(int col, int row) {
        // TODO: return sheet.getSubimage(col * frameWidth, row * frameHeight, frameWidth, frameHeight)
        return null;
    }

    /**
     * Lấy nhiều frame liên tiếp trên cùng 1 hàng.
     * @param row hàng cần lấy
     * @param count số frame cần lấy
     * @return mảng BufferedImage[]
     */
    public BufferedImage[] getRow(int row, int count) {
        // TODO: Tạo mảng, lặp gọi getFrame(i, row)
        return null;
    }

    /**
     * Lấy toàn bộ frame trên 1 hàng.
     */
    public BufferedImage[] getFullRow(int row) {
        // TODO: Tính số cột = sheet.getWidth() / frameWidth, gọi getRow(row, cols)
        return null;
    }
}
