package TerraIncognita.entity;

/**
 * Enum trạng thái hoạt động của entity.
 * Dùng để chọn bộ frame animation phù hợp.
 */
public enum EntityState {
    IDLE,       // Đứng yên
    WALK,       // Đang di chuyển
    ATTACK,     // Đang tấn công
    HURT,       // Bị trúng đòn
    DEAD;       // Đã chết
}
