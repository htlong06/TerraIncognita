package TerraIncognita.entity;

import TerraIncognita.util.Constants;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Mũi tên bắn ra từ player khi dùng cung.
 *
 * Mũi tên bay theo hướng từ tâm player tới vị trí chuột lúc bắn,
 * với tốc độ không đổi (ARROW_SPEED). Sprite được xoay theo góc bay.
 * Khi chạm quái (hitbox intersect) hoặc bay quá xa (MAX_RANGE) thì biến mất.
 */
public class Arrow {

    private double worldX;      // tâm mũi tên (pixel)
    private double worldY;
    private double velX;        // vận tốc theo X (pixel/giây)
    private double velY;        // vận tốc theo Y
    private double angle;       // góc bay (radian), dùng để xoay sprite
    private double distanceTraveled; // quãng đường đã bay (pixel)
    private boolean alive;      // false khi đã trúng mục tiêu hoặc bay quá xa

    private BufferedImage sprite;   // frame mũi tên (arrow0)
    private int spriteSize;         // kích thước vẽ (pixel)

    // Hitbox của mũi tên nhỏ hơn sprite — chỉ phần đầu nhọn
    private static final int HITBOX_SIZE = 12;

    public Arrow(double startX, double startY, double targetX, double targetY, BufferedImage sprite) {
        this.worldX = startX;
        this.worldY = startY;
        this.sprite = sprite;
        this.spriteSize = Constants.ARROW_SPRITE_SIZE;
        this.alive = true;
        this.distanceTraveled = 0;

        // Tính hướng bay (vector đơn vị từ start → target)
        double dx = targetX - startX;
        double dy = targetY - startY;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1) {
            // Chuột trùng tâm player → bắn sang phải mặc định
            dx = 1;
            dy = 0;
            length = 1;
        }
        this.angle = Math.atan2(dy, dx);
        this.velX = (dx / length) * Constants.ARROW_SPEED;
        this.velY = (dy / length) * Constants.ARROW_SPEED;
    }

    /**
     * Cập nhật vị trí mũi tên mỗi frame.
     */
    public void update(double deltaTime) {
        if (!alive) return;

        double moveX = velX * deltaTime;
        double moveY = velY * deltaTime;
        worldX += moveX;
        worldY += moveY;
        distanceTraveled += Math.sqrt(moveX * moveX + moveY * moveY);

        // Tự hủy nếu bay quá xa hoặc ra ngoài màn hình
        if (distanceTraveled > Constants.ARROW_MAX_RANGE) {
            alive = false;
        }
        if (worldX < -50 || worldX > Constants.SCREEN_WIDTH + 50
                || worldY < -50 || worldY > Constants.SCREEN_HEIGHT + 50) {
            alive = false;
        }
    }

    /**
     * Vẽ mũi tên lên màn hình, xoay sprite theo góc bay.
     */
    public void render(Graphics2D g2d) {
        if (!alive || sprite == null) return;

        int drawX = (int) worldX - spriteSize / 2;
        int drawY = (int) worldY - spriteSize / 2;

        AffineTransform old = g2d.getTransform();
        g2d.rotate(angle, worldX, worldY);
        g2d.drawImage(sprite, drawX, drawY, spriteSize, spriteSize, null);
        g2d.setTransform(old);
    }

    /**
     * Hitbox tại vị trí hiện tại — hình vuông nhỏ ở tâm mũi tên.
     */
    public Rectangle getHitbox() {
        int half = HITBOX_SIZE / 2;
        return new Rectangle(
                (int) worldX - half,
                (int) worldY - half,
                HITBOX_SIZE,
                HITBOX_SIZE
        );
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public double getAngle() { return angle; }
}
