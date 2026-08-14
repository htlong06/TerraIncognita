package TerraIncognita.quest;

/**
 * Tiến độ của MỘT quest đã được player nhận — tách riêng khỏi {@link Quest}
 * (khuôn mẫu bất biến) vì mỗi lần player nhận quest cần 1 bộ đếm progress
 * độc lập, không đụng vào định nghĩa gốc.
 */
public class QuestProgress {

    private final Quest quest;
    private int currentAmount;
    private QuestStatus status;

    public QuestProgress(Quest quest) {
        this.quest = quest;
        this.currentAmount = 0;
        this.status = QuestStatus.ACTIVE;
    }

    /**
     * Cộng thêm tiến độ (VD: vừa giết 1 quái khớp targetId).
     * Tự chuyển sang READY_TO_TURN_IN khi đủ số lượng.
     */
    public void addProgress(int amount) {
        if (status != QuestStatus.ACTIVE) return; // đã xong hoặc đã trả thưởng, không cộng nữa
        currentAmount = Math.min(currentAmount + amount, quest.getTargetAmount());
        if (currentAmount >= quest.getTargetAmount()) {
            status = QuestStatus.READY_TO_TURN_IN;
        }
    }

    public Quest getQuest() { return quest; }
    public int getCurrentAmount() { return currentAmount; }
    public QuestStatus getStatus() { return status; }
    public boolean isReadyToTurnIn() { return status == QuestStatus.READY_TO_TURN_IN; }

    public void markTurnedIn() { this.status = QuestStatus.TURNED_IN; }
}
