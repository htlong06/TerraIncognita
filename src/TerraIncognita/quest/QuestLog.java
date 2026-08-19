package TerraIncognita.quest;

import TerraIncognita.inventory.Inventory;
import TerraIncognita.item.Item;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sổ nhiệm vụ của 1 player — theo dõi quest nào đang làm, tiến độ tới đâu,
 * quest nào
 * đã trả thưởng xong.
 */
public class QuestLog {

    private final Map<String, QuestProgress> activeQuests; // key = quest id
    private final Set<String> turnedInQuestIds;

    public QuestLog() {
        this.activeQuests = new LinkedHashMap<>(); // giữ thứ tự nhận quest
        this.turnedInQuestIds = new HashSet<>();
    }

    /**
     * Nhận 1 quest mới. Không cho nhận nếu đã đang làm hoặc đã trả thưởng rồi.
     * 
     * @return true nếu nhận thành công
     */
    public boolean acceptQuest(Quest quest) {
        if (isActive(quest.getId()) || isTurnedIn(quest.getId())) {
            return false;
        }
        activeQuests.put(quest.getId(), new QuestProgress(quest));
        return true;
    }

    public boolean isActive(String questId) {
        return activeQuests.containsKey(questId);
    }

    public boolean isTurnedIn(String questId) {
        return turnedInQuestIds.contains(questId);
    }

    public QuestProgress getProgress(String questId) {
        return activeQuests.get(questId);
    }

    /**
     * Gọi mỗi khi player giết 1 quái — cộng tiến độ cho MỌI quest đang active
     * có objective KILL_MONSTER khớp tên quái. Gọi tại nơi quái chết trong
     * GameEngine (chỗ đang cộng exp/gold cho player).
     * 
     * @param monsterName tên quái (Entity.getName(), VD: "Orc", "Slime")
     */
    public void notifyMonsterKilled(String monsterName) {
        for (QuestProgress qp : activeQuests.values()) {
            Quest q = qp.getQuest();
            if (q.getObjectiveType() == QuestObjectiveType.KILL_MONSTER
                    && q.getTargetId().equals(monsterName)) {
                qp.addProgress(1);
            }
        }
    }

    /**
     * Kiểm tra 1 quest COLLECT_ITEM đã đủ điều kiện trả thưởng chưa, bằng
     * cách soi trực tiếp vào Inventory hiện tại (không cần track sự kiện
     * nhặt đồ riêng — vật phẩm có thể tới từ rương, quái rơi, hay mua ở shop,
     * đều tính miễn túi đồ có đủ số lượng lúc quay lại trả nhiệm vụ).
     */
    public boolean checkCollectObjective(String questId, Inventory inventory) {
        QuestProgress qp = activeQuests.get(questId);
        if (qp == null || qp.getQuest().getObjectiveType() != QuestObjectiveType.COLLECT_ITEM) {
            return false;
        }
        Item found = inventory.findById(qp.getQuest().getTargetId());
        int have = (found != null) ? Math.max(found.getStackCount(), 1) : 0;
        qp.addProgress(Math.max(0, have - qp.getCurrentAmount())); // đồng bộ progress hiển thị
        return qp.isReadyToTurnIn();
    }

    /**
     * Trả thưởng: xoá quest khỏi active, đánh dấu đã trả. Việc trừ item
     * (nếu COLLECT_ITEM) và cộng gold/exp/item thưởng do caller (QuestGiver/
     * GameEngine) thực hiện, vì QuestLog không nên tự ý đụng vào Inventory
     * của player để trừ đồ.
     */
    public void markTurnedIn(String questId) {
        QuestProgress qp = activeQuests.remove(questId);
        if (qp != null) {
            turnedInQuestIds.add(questId);
        }
    }

    public Map<String, QuestProgress> getActiveQuests() {
        return activeQuests;
    }
}
