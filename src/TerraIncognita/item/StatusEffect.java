package TerraIncognita.item;

import TerraIncognita.entity.Entity;

/**
 * Hiệu ứng trạng thái — áp dụng lên Entity trong khoảng thời gian.
 *
 * Ví dụ: Poison (mất máu dần), Slow (giảm speed), Buff (tăng ATK tạm thời).
 * Mỗi Entity có danh sách activeEffects, cập nhật mỗi frame.
 */
public class StatusEffect {

    public enum EffectType {
        POISON, SLOW, ATK_BUFF, DEF_BUFF, SPEED_BUFF, REGEN
    }

    private EffectType type;
    private String name;
    private double duration;            // thời gian còn lại (giây)
    private double totalDuration;       // tổng thời gian (giây)
    private int value;                  // giá trị hiệu ứng
    private double tickInterval;        // khoảng cách giữa các lần tick (giây)
    private double tickTimer;           // bộ đếm tick

    public StatusEffect(String name, double duration, int value) {
        this.name = name;
        this.duration = duration;
        this.totalDuration = duration;
        this.value = value;
        this.tickInterval = 1.0;    // tick mỗi giây mặc định
        this.tickTimer = 0;
        this.type = EffectType.POISON;  // mặc định
    }

    public StatusEffect(String name, EffectType type, double duration, int value) {
        this(name, duration, value);
        this.type = type;
    }

    /**
     * Cập nhật hiệu ứng mỗi frame.
     * @param deltaTime thời gian frame (giây)
     * @param target entity đang chịu hiệu ứng
     */
    public void update(double deltaTime, Entity target) {
        duration -= deltaTime;
        tickTimer += deltaTime;

        if (tickTimer >= tickInterval) {
            tickTimer -= tickInterval;
            // Apply effect
            switch (type) {
                case POISON:
                    target.takeDamage(value);
                    break;
                case REGEN:
                    target.heal(value);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Kiểm tra hiệu ứng đã hết thời gian chưa.
     */
    public boolean isExpired() {
        return duration <= 0;
    }

    // --- Getter ---
    public EffectType getType() { return type; }
    public String getName() { return name; }
    public int getValue() { return value; }
    public double getRemainingDuration() { return duration; }
    public double getTotalDuration() { return totalDuration; }
}
