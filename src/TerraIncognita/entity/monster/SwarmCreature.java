package TerraIncognita.entity.monster;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
import TerraIncognita.util.Vec2;

/**
 * 1 quái trong bầy (Swarm Event) — tương đương class "Boid" trong ví dụ
 * chp06_agents/NOC_6_09_Flocking (The Nature of Code, Daniel Shiffman):
 * có velocity + acceleration dạng vector, nhận lực qua applyForce(), giới
 * hạn bởi maxSpeed/maxForce. Toàn bộ lực (separate/align/cohesion/flee/
 * contain) do {@link TerraIncognita.event.SwarmEvent} tính toán mỗi frame
 * (tương đương "Flock" quản lý danh sách "Boid" trong tài liệu gốc) rồi
 * gọi applyForce() — vì vậy {@link #updateAI} ở đây bị ghi đè thành no-op
 * để không bị AI chase/attack mặc định của Monster can thiệp.
 *
 * Nhận sát thương BÌNH THƯỜNG từ kiếm/cung/bomb như mọi quái khác (dùng
 * thẳng takeDamage() kế thừa từ Monster, không override) — vì di chuyển
 * né tránh liên tục nên trên thực tế bomb (gây sát thương diện rộng) vẫn
 * là cách hiệu quả nhất để dọn cả bầy, nhưng player hoàn toàn có thể chém/
 * bắn hạ từng con nếu bắt kịp.
 *
 * Hiện chưa có sprite riêng — không gọi initAnimations() nên
 * currentAnimation luôn null, khiến GameEngine.drawMonster() tự động vẽ
 * fallback "khối vuông màu đỏ" có sẵn — đúng yêu cầu tạm thời ban đầu.
 * Sau này có sprite riêng, chỉ cần gọi initAnimations(assetLoader) lúc
 * spawn (giống OrcMonster) là tự động chuyển sang vẽ sprite thật.
 */
public class SwarmCreature extends Monster {

    // location (vị trí) đã có sẵn ở Entity.worldX/worldY — không lặp lại ở đây.
    private final Vec2 velocity;
    private final Vec2 acceleration;
    private double maxSpeed; // giới hạn tốc độ hiện tại — SwarmEvent chỉnh động theo khoảng cách tới player (bình thường/hoảng loạn)
    private final double maxForce; // giới hạn độ lớn lực steering mỗi behavior — cố định, lấy từ Constants

    public SwarmCreature(int tileX, int tileY) {
        super("Swarm Creature", Constants.SWARM_HP, 0, 0, tileX, tileY);
        this.expReward = Constants.SWARM_EXP_REWARD;
        this.goldReward = Constants.SWARM_GOLD_REWARD;
        this.speed = Constants.SWARM_BASE_SPEED;
        this.detectionRange = 0; // không dùng tới — AI mặc định đã bị vô hiệu hoá bên dưới

        this.velocity = new Vec2(0, 0);
        this.acceleration = new Vec2(0, 0);
        this.maxSpeed = Constants.SWARM_BASE_SPEED;
        this.maxForce = Constants.SWARM_MAX_FORCE;

        // Hitbox nhỏ hơn quái thường (mặc định 24x24) — đúng tinh thần "quái nhỏ"
        setHitbox(10, 10, 12, 12);
    }

    /**
     * Ghi đè có chủ đích: bỏ qua toàn bộ AIState (IDLE/CHASE/ATTACK) mặc
     * định của Monster. Chuyển động thật nằm ở SwarmEvent.update().
     */
    @Override
    public void updateAI(Player player, GameMap map, double deltaTime) {
        // no-op
    }

    /** acceleration.add(force) — giống hệt Boid.applyForce() trong NOC. */
    public void applyForce(Vec2 force) {
        acceleration.add(force);
    }

    public Vec2 getVelocity() { return velocity; }
    public Vec2 getAcceleration() { return acceleration; }

    public void setVelocity(double x, double y) {
        this.velocity.x = x;
        this.velocity.y = y;
    }

    public void resetAcceleration() {
        acceleration.x = 0;
        acceleration.y = 0;
    }

    public double getMaxSpeed() { return maxSpeed; }
    public double getMaxForce() { return maxForce; }

    /** SwarmEvent gọi mỗi frame để tăng tốc độ tối đa khi quái đang hoảng loạn (né player/bomb). */
    public void setMaxSpeed(double maxSpeed) { this.maxSpeed = maxSpeed; }
}
