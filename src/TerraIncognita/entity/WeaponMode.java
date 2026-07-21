package TerraIncognita.entity;

/**
 * Chế độ vũ khí hiện tại của Player.
 * SWORD — cận chiến, có combo chuỗi (đánh 3 lần liên tiếp ra đòn mạnh hơn).
 * BOW   — tầm xa, giữ chuột trái để ngắm (hiện đường kẻ mờ), thả ra để bắn.
 *
 * Chuyển đổi qua lại bằng phím E (khi không đứng cạnh merchant — cạnh
 * merchant thì E dùng để tương tác/mở shop như cũ).
 */
public enum WeaponMode {
    SWORD,
    BOW
}
