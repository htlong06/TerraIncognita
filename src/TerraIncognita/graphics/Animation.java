package TerraIncognita.graphics;

import java.awt.image.BufferedImage;

/**
 * Quản lý animation từ mảng frame ảnh.
 *
 * Giữ: mảng frame hiện tại, tốc độ chuyển frame, trạng thái loop.
 * Được cập nhật mỗi frame trong game loop, tự động chuyển sang frame tiếp theo
 * khi đủ thời gian.
 *
 * Cách dùng:
 *   Animation walkAnim = new Animation(walkFrames, 150); // đổi frame mỗi 150ms
 *   walkAnim.update(deltaTime);
 *   g2d.drawImage(walkAnim.getCurrentFrame(), x, y, null);
 */
public class Animation {

    private BufferedImage[] frames;         // mảng frame ảnh
    private int currentFrameIndex;          // chỉ số frame đang hiển thị
    private double frameDuration;           // thời gian mỗi frame (giây)
    private double elapsedTime;             // thời gian đã trôi qua
    private boolean looping;                // có lặp lại không
    private boolean finished;               // animation đã kết thúc chưa

    /**
     * @param frames mảng frame ảnh
     * @param frameDurationMs thời gian mỗi frame tính bằng milliseconds
     */
    public Animation(BufferedImage[] frames, int frameDurationMs) {
        this.frames = frames;
        this.frameDuration = frameDurationMs / 1000.0;
        this.currentFrameIndex = 0;
        this.elapsedTime = 0;
        this.looping = true;
        this.finished = false;
    }

    /**
     * Cập nhật animation theo thời gian.
     * @param deltaTime thời gian (giây) từ frame trước
     */
    public void update(double deltaTime) {
        if (finished || frames == null || frames.length == 0) return;

        elapsedTime += deltaTime;
        if (elapsedTime >= frameDuration) {
            elapsedTime -= frameDuration;
            currentFrameIndex++;

            if (currentFrameIndex >= frames.length) {
                if (looping) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frames.length - 1;
                    finished = true;
                }
            }
        }
    }

    /**
     * Lấy frame hiện tại để vẽ.
     */
    public BufferedImage getCurrentFrame() {
        if (frames == null || frames.length == 0) return null;
        return frames[currentFrameIndex];
    }

    /**
     * Reset animation về frame đầu tiên.
     */
    public void reset() {
        currentFrameIndex = 0;
        elapsedTime = 0;
        finished = false;
    }

    /**
     * Đặt looping.
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /**
     * Kiểm tra animation đã kết thúc chưa (chỉ có ý nghĩa khi looping = false).
     */
    public boolean isFinished() {
        return finished;
    }

    public int getFrameCount() {
        return frames != null ? frames.length : 0;
    }
}
