package TerraIncognita.quest;

/**
 * Loại mục tiêu của 1 nhiệm vụ (quest).
 * Muốn thêm loại nhiệm vụ mới (VD: đi tới vị trí, nói chuyện với NPC khác...)
 * chỉ cần thêm 1 giá trị enum ở đây, rồi xử lý nó trong QuestLog.
 */
public enum QuestObjectiveType {
    /** Giết N con quái có tên (Entity.name) khớp targetId. VD: targetId="Orc". */
    KILL_MONSTER,

    /** Thu thập/mang theo N item có id khớp targetId (kiểm tra qua Inventory). */
    COLLECT_ITEM
}
