package TerraIncognita.graphics;

import java.awt.image.BufferedImage;

/**
 * Cắt sprite sheet (1 ảnh lớn chứa nhiều frame) thành mảng BufferedImage[].
 *
 * Sprite sheet là cách tổ chức phổ biến: gộp nhiều frame animation vào 1 file
 * PNG,
 * mỗi frame có kích thước cố định (ví dụ 32x32), xếp theo hàng/cột.
 *
 * Cách dùng:
 * SpriteSheet sheet = new SpriteSheet(ImageIO.read(...), 32, 32);
 * BufferedImage frame = sheet.getFrame(col, row);
 * BufferedImage[] walkFrames = sheet.getRow(0, 4); // 4 frame ở hàng 0
 */
public class SpriteSheet {

    private BufferedImage sheet; // ảnh gốc (sprite sheet)
    private int frameWidth; // chiều rộng 1 frame (pixel)
    private int frameHeight; // chiều cao 1 frame (pixel)

    /**
     * @param sheet       ảnh sprite sheet đã load
     * @param frameWidth  chiều rộng mỗi frame (pixel)
     * @param frameHeight chiều cao mỗi frame (pixel)
     */
    public SpriteSheet(BufferedImage sheet, int frameWidth, int frameHeight) {
        this.sheet = sheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    /**
     * Lấy 1 frame tại vị trí (col, row) trên sprite sheet.
     * 
     * @param col cột (0-indexed)
     * @param row hàng (0-indexed)
     * @return BufferedImage frame cắt ra
     */
    public BufferedImage getFrame(int col, int row) {
        if (sheet == null)
            return null;
        int x = col * frameWidth;
        int y = row * frameHeight;
        if (x < 0 || y < 0 || x + frameWidth > sheet.getWidth() || y + frameHeight > sheet.getHeight()) {
            return null;
        }
        return sheet.getSubimage(x, y, frameWidth, frameHeight);
    }

    /**
     * Lấy nhiều frame liên tiếp trên cùng 1 hàng.
     * 
     * @param row   hàng cần lấy
     * @param count số frame cần lấy
     * @return mảng BufferedImage[]
     */
    public BufferedImage[] getRow(int row, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            frames[i] = getFrame(i, row);
        }
        return frames;
    }

    /**
     * Lấy toàn bộ frame trên 1 hàng.
     */
    public BufferedImage[] getFullRow(int row) {
        if (sheet == null || frameWidth <= 0)
            return new BufferedImage[0];
        int cols = sheet.getWidth() / frameWidth;
        return getRow(row, cols);
    }
}