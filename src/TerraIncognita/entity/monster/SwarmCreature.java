package TerraIncognita.entity.monster;

import java.awt.image.BufferedImage;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
import TerraIncognita.util.Vec2;

/**
 * 1 quái trong bầy (Swarm Event)
 */
public class SwarmCreature extends Monster {

    private static final int IDLE_FRAME_MS = 200;
    private static final int WALK_FRAME_MS = 120;

    private final Vec2 velocity;
    private final Vec2 acceleration;
    private double maxSpeed; // giới hạn tốc độ hiện tại
    private final double maxForce; // giới hạn độ lớn lực steering mỗi behavior

    public SwarmCreature(int tileX, int tileY) {
        super("Swarm Creature", Constants.SWARM_HP, 0, 0, tileX, tileY);
        this.expReward = Constants.SWARM_EXP_REWARD;
        this.goldReward = Constants.SWARM_GOLD_REWARD;
        this.speed = Constants.SWARM_BASE_SPEED;
        this.detectionRange = 0;

        this.velocity = new Vec2(0, 0);
        this.acceleration = new Vec2(0, 0);
        this.maxSpeed = Constants.SWARM_BASE_SPEED;
        this.maxForce = Constants.SWARM_MAX_FORCE;

        setHitbox(10, 10, 12, 12);
    }

    /**
     * Khởi tạo animation frog cho SwarmCreature.
     * - IDLE: dùng frog_idle_right/left (cột 0-7) — ếch đứng yên
     * - WALK: dùng frog_walk_right/left (cột 8-13) — ếch nhảy/di chuyển
     */
    public void initAnimations(AssetLoader assets) {
        BufferedImage[] idleRight = assets.getFrames("frog_idle_right");
        BufferedImage[] idleLeft = assets.getFrames("frog_idle_left");
        BufferedImage[] walkRight = assets.getFrames("frog_walk_right");
        BufferedImage[] walkLeft = assets.getFrames("frog_walk_left");

        // Đứng yên: frame 0-7
        addDirectionalAnimations(EntityState.IDLE, idleRight, idleLeft, IDLE_FRAME_MS, true);
        // Di chuyển (nhảy): frame 8-13
        addDirectionalAnimations(EntityState.WALK, walkRight, walkLeft, WALK_FRAME_MS, true);
        // Bị đánh: dùng frame idle
        addDirectionalAnimations(EntityState.HURT, idleRight, idleLeft, WALK_FRAME_MS, false);

        useAnimation(EntityState.IDLE, getDirection());
    }

    /**
     * bỏ qua toàn bộ AIState (IDLE/CHASE/ATTACK) mặc định của Monster.
     */
    @Override
    public void updateAI(Player player, GameMap map, double deltaTime) {
        // no-op
    }

    /** acceleration.add(force) — giống hệt Boid.applyForce() trong NOC. */
    public void applyForce(Vec2 force) {
        acceleration.add(force);
    }

    public Vec2 getVelocity() {
        return velocity;
    }

    public Vec2 getAcceleration() {
        return acceleration;
    }

    public void setVelocity(double x, double y) {
        this.velocity.x = x;
        this.velocity.y = y;
    }

    public void resetAcceleration() {
        acceleration.x = 0;
        acceleration.y = 0;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMaxForce() {
        return maxForce;
    }

    /**
     * SwarmEvent gọi mỗi frame để tăng tốc độ tối đa khi quái đang hoảng loạn (né
     * player/bomb).
     */
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
