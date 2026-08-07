package TerraIncognita.entity;

import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Bom — đặt bởi player (phím B), tồn tại vĩnh viễn trên map cho tới khi
 * bị kích nổ. Hiện tại vẽ tạm bằng 1 khối vuông màu đỏ (chưa có sprite).
 *
 * Kích nổ khi:
 *  - Va chạm hitbox với bất kỳ thực thể nào NGOẠI TRỪ player (VD quái vật).
 *  - Bị mũi tên (Arrow) bắn trúng — mũi tên cũng nổ tung và biến mất
 *    giống như khi bắn trúng quái vật thông thường.
 * Bom KHÔNG tự nổ khi va chạm với bom khác.
 *
 * Khi nổ, vùng ảnh hưởng là hình vuông BOMB_EXPLOSION_TILES x
 * BOMB_EXPLOSION_TILES ô, tâm tại ô bom đang đứng. Player vẫn nhận sát
 * thương nếu đứng trong phạm vi nổ, dù không phải là thứ kích hoạt vụ nổ.
 */
public class Bomb {

    public enum BombState {
        PLACED,     // đang nằm yên trên map, chờ va chạm
        EXPLODING,  // vừa nổ, đang hiển thị hiệu ứng trong ExplosionDuration giây
        GONE        // đã kết thúc, cần bị xoá khỏi danh sách active
    }

    private double worldX; // góc trên-trái của khối vuông (pixel)
    private double worldY;
    private BombState state;
    private double explosionTimer; // đếm ngược trong lúc EXPLODING

    public Bomb(double worldX, double worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.state = BombState.PLACED;
        this.explosionTimer = 0.0;
    }

    /**
     * Cập nhật mỗi frame — chỉ có việc phải làm khi đang trong lúc nổ
     * (đếm ngược hiệu ứng rồi chuyển sang GONE).
     */
    public void update(double deltaTime) {
        if (state == BombState.EXPLODING) {
            explosionTimer -= deltaTime;
            if (explosionTimer <= 0) {
                state = BombState.GONE;
            }
        }
    }

    /**
     * Kích hoạt vụ nổ. Không làm gì nếu bom đã nổ hoặc đã biến mất
     * (tránh nổ 2 lần do nhiều nguồn va chạm trong cùng 1 frame).
     */
    public void explode() {
        if (state != BombState.PLACED) return;
        state = BombState.EXPLODING;
        explosionTimer = Constants.BOMB_EXPLOSION_DURATION;
    }

    /**
     * Hitbox dùng để phát hiện va chạm kích nổ (kích thước khối vuông đặt bom).
     */
    public Rectangle getHitbox() {
        return new Rectangle(
                (int) Math.round(worldX),
                (int) Math.round(worldY),
                Constants.BOMB_SIZE,
                Constants.BOMB_SIZE
        );
    }

    /**
     * Vùng ảnh hưởng khi nổ — hình vuông BOMB_EXPLOSION_TILES x
     * BOMB_EXPLOSION_TILES ô, tâm là ô mà bom đang đứng (không phải tâm
     * hitbox nhỏ của khối vuông, mà tâm theo lưới tile để phạm vi nổ
     * luôn thẳng hàng với lưới map).
     */
    public Rectangle getExplosionArea() {
        int ts = Constants.TILE_SIZE;
        int centerTileX = (int) Math.floor((worldX + Constants.BOMB_SIZE / 2.0) / ts);
        int centerTileY = (int) Math.floor((worldY + Constants.BOMB_SIZE / 2.0) / ts);
        int half = Constants.BOMB_EXPLOSION_TILES / 2;

        int areaX = (centerTileX - half) * ts;
        int areaY = (centerTileY - half) * ts;
        int areaSize = Constants.BOMB_EXPLOSION_TILES * ts;

        return new Rectangle(areaX, areaY, areaSize, areaSize);
    }

    /**
     * Vẽ bom: khối vuông đỏ khi PLACED, khối cam lớn hơn (vùng nổ) khi
     * đang EXPLODING. Không vẽ gì khi đã GONE.
     */
    public void render(Graphics2D g2d) {
        if (state == BombState.PLACED) {
            g2d.setColor(Color.RED);
            g2d.fillRect((int) Math.round(worldX), (int) Math.round(worldY),
                    Constants.BOMB_SIZE, Constants.BOMB_SIZE);
        } else if (state == BombState.EXPLODING) {
            Rectangle area = getExplosionArea();
            // Alpha giảm dần theo thời gian còn lại để có cảm giác hiệu ứng tắt dần
            int alpha = (int) (200 * Math.max(0, explosionTimer / Constants.BOMB_EXPLOSION_DURATION));
            g2d.setColor(new Color(255, 140, 0, alpha));
            g2d.fillRect(area.x, area.y, area.width, area.height);
        }
    }

    public BombState getState() {
        return state;
    }

    public boolean isAlive() {
        return state != BombState.GONE;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }
}
