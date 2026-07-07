package TerraIncognita.entity.npc;

import TerraIncognita.entity.Player;

/**
 * NPC Thợ rèn — nâng cấp vũ khí/giáp.
 * Tốn vàng hoặc nguyên liệu để tăng chỉ số ATK/DEF.
 */
public class Blacksmith extends NPC {

    public Blacksmith(int tileX, int tileY) {
        super("Blacksmith", tileX, tileY);
    }

    @Override
    public void interact(Player player) {
        // TODO: Mở giao diện nâng cấp trang bị
        // TODO: Hiện danh sách equipment đang mang → chọn → trả vàng → tăng chỉ số
    }

    // TODO: Phương thức nâng cấp
    // public boolean upgradeEquipment(Player player, Equipment equipment) { ... }
}
