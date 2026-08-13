package TerraIncognita.quest;

/**
 * Trạng thái của 1 nhiệm vụ trong QuestLog của player.
 */
public enum QuestStatus {
    ACTIVE,           // đã nhận, chưa đủ điều kiện hoàn thành
    READY_TO_TURN_IN, // đã đủ điều kiện, chờ quay lại NPC để nhận thưởng
    TURNED_IN         // đã trả thưởng xong (không cho nhận lại)
}
