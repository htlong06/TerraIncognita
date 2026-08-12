package TerraIncognita.entity;

import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Bom — đặt bởi player (phím B), tồn tại vĩnh viễn trên map cho tới khi
 * bị kích nổ. Vẽ bằng animation (asset "explosion-b": frame 0 = quả bom
 * lúc chưa nổ, frame 1-10 = các giai đoạn nổ) — xem AssetLoader.loadBomb().
 * Nếu animation chưa load được vì lý do gì đó, tự động rơi về vẽ hình khối
 * đơn giản (khối đỏ / vuông cam) như bản gốc, để không bao giờ vẽ ra khoảng
 * trống nếu asset lỗi.
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
        EXPLODING,  // vừa nổ, đang chạy animation nổ
        GONE        // animation nổ đã chạy xong, cần bị xoá khỏi danh sách active
    }

    private double worldX; // góc trên-trái của hitbox đặt bom (pixel)
    private double worldY;
    private BombState state;
    private double explosionTimer; // dự phòng: dùng khi KHÔNG có animation nổ để đếm ngược

    private Animation idleAnimation;      // 1 frame tĩnh: quả bom lúc chưa nổ
    private Animation explosionAnimation; // 10 frame: animation lúc nổ
    private BufferedImage bombPlacedImage; // bomb.png — hình thùng bom pixel art

    public Bomb(double worldX, double worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.state = BombState.PLACED;
        this.explosionTimer = 0.0;
    }

    /**
     * Nạp animation từ AssetLoader. Gọi ngay sau khi tạo Bomb (trong
     * GameEngine.placeBomb()), giống cách Player.initAnimations() hoạt động.
     * Nếu asset "bomb_idle"/"bomb_explosion" không có frame nào (load lỗi),
     * animation tương ứng sẽ là null và render() tự rơi về vẽ hình khối.
     */
    public void initAnimations(AssetLoader assetLoader) {
        // Ưu tiên dùng bomb.png (hình thùng bom pixel art)
        this.bombPlacedImage = assetLoader.getTile("bomb_placed");

        BufferedImage[] idleFrames = assetLoader.getFrames("bomb_idle");
        if (idleFrames != null && idleFrames.length > 0) {
            this.idleAnimation = new Animation(idleFrames, 1000); // 1 frame tĩnh, tốc độ không quan trọng
        }

        BufferedImage[] explosionFrames = assetLoader.getFrames("bomb_explosion");
        if (explosionFrames != null && explosionFrames.length > 0) {
            this.explosionAnimation = new Animation(explosionFrames, Constants.BOMB_EXPLOSION_FRAME_MS);
            this.explosionAnimation.setLooping(false); // nổ xong dừng lại ở frame cuối, không lặp lại
        }
    }

    /**
     * Cập nhật mỗi frame — chỉ có việc phải làm khi đang trong lúc nổ:
     * chạy animation nổ tới khi kết thúc rồi chuyển sang GONE. Nếu animation
     * nổ không có sẵn, rơi về đếm ngược timer cũ (BOMB_EXPLOSION_DURATION).
     */
    public void update(double deltaTime) {
        if (state != BombState.EXPLODING) return;

        if (explosionAnimation != null) {
            explosionAnimation.update(deltaTime);
            if (explosionAnimation.isFinished()) {
                state = BombState.GONE;
            }
        } else {
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
        if (explosionAnimation != null) {
            explosionAnimation.reset();
        }
    }

    /**
     * Hitbox dùng để phát hiện va chạm kích nổ (kích thước khối vuông đặt bom,
     * KHÔNG phụ thuộc kích thước sprite vẽ ra — animation chỉ ảnh hưởng hiển thị).
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
     * Vùng render của animation nổ: nâng lên trên mặt đất một chút để hiệu ứng
     * không bị "bám" quá sát nền và trông hơn hẳn.
     */
    public Rectangle getExplosionRenderBounds() {
        Rectangle area = getExplosionArea();
        return new Rectangle(
                area.x,
                area.y + Constants.BOMB_EXPLOSION_RENDER_OFFSET_Y,
                area.width,
                area.height
        );
    }

    /**
     * Vẽ bom: sprite animation nếu có (idle lúc PLACED, animation nổ lúc
     * EXPLODING — vẽ phủ kín getExplosionArea()). Nếu asset không load được,
     * rơi về hình khối đơn giản như bản gốc.
     */
    public void render(Graphics2D g2d) {
        if (state == BombState.PLACED) {
            int drawSize = Constants.BOMB_IDLE_DRAW_SIZE;
            int centerX = (int) Math.round(worldX + Constants.BOMB_SIZE / 2.0);
            int centerY = (int) Math.round(worldY + Constants.BOMB_SIZE / 2.0);

            if (bombPlacedImage != null) {
                // Ưu tiên vẽ bomb.png (hình thùng bom pixel art)
                int drawX = centerX - drawSize / 2;
                int drawY = centerY - drawSize / 2;
                g2d.drawImage(bombPlacedImage, drawX, drawY, drawSize, drawSize, null);
            } else if (idleAnimation != null && idleAnimation.getCurrentFrame() != null) {
                BufferedImage frame = idleAnimation.getCurrentFrame();
                int drawHeight = (int) Math.round(drawSize * (double) frame.getHeight() / frame.getWidth());
                int drawX = centerX - drawSize / 2;
                int drawY = centerY - drawHeight / 2;
                g2d.drawImage(frame, drawX, drawY, drawSize, drawHeight, null);
            } else {
                // Fallback: khối vuông đỏ (asset chưa load được)
                g2d.setColor(Color.RED);
                g2d.fillRect((int) Math.round(worldX), (int) Math.round(worldY),
                        Constants.BOMB_SIZE, Constants.BOMB_SIZE);
            }
        } else if (state == BombState.EXPLODING) {
            Rectangle area = getExplosionRenderBounds();
            if (explosionAnimation != null && explosionAnimation.getCurrentFrame() != null) {
                BufferedImage frame = explosionAnimation.getCurrentFrame();
                // Vẽ phủ kín toàn bộ vùng ảnh hưởng (kéo giãn cho khớp ô vuông area)
                g2d.drawImage(frame, area.x, area.y, area.width, area.height, null);
            } else {
                // Fallback: khối cam mờ dần (asset chưa load được)
                int alpha = (int) (200 * Math.max(0, explosionTimer / Constants.BOMB_EXPLOSION_DURATION));
                g2d.setColor(new Color(255, 140, 0, alpha));
                g2d.fillRect(area.x, area.y, area.width, area.height);
            }
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
