package TerraIncognita.entity.npc;

import TerraIncognita.entity.Player;

/**
 * NPC cho nhiệm vụ — hiện bảng quest, kiểm tra hoàn thành, trả thưởng.
 */
public class QuestGiver extends NPC {

    // TODO: Khai báo các trường
    // - List<Quest> availableQuests
    // - List<Quest> activeQuests

    public QuestGiver(int tileX, int tileY) {
        super("Quest Giver", tileX, tileY);
    }

    @Override
    public void interact(Player player) {
        // TODO: Hiện danh sách quest
        // TODO: Kiểm tra quest đã hoàn thành → trả thưởng
    }
}
