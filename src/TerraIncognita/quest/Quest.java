package TerraIncognita.quest;

import TerraIncognita.item.Item;

/**
 * Định nghĩa 1 nhiệm vụ (quest) — coi như "khuôn mẫu" bất biến, khai báo 1 lần
 * (thường trong QuestGiver.initQuests(), giống cách Merchant.initShop() khai
 * báo hàng bán). Muốn thêm/sửa nhiệm vụ chỉ cần sửa ở nơi khai báo, KHÔNG
 * cần đụng vào logic của QuestLog/QuestGiver.
 *
 * Ví dụ khai báo 1 quest "giết 3 Orc":
 * <pre>
 * Quest q = new Quest(
 *     "kill_orcs",                       // id duy nhất
 *     "Dọn dẹp lũ Orc",                   // tiêu đề hiện trên bảng quest
 *     "Có 3 con Orc đang quấy nhiễu\nkhu vực này. Hãy tiêu diệt chúng.", // lời NPC giao việc
 *     "Cảm ơn! Đây là phần thưởng của bạn.",  // lời NPC khi trả thưởng
 *     QuestObjectiveType.KILL_MONSTER,
 *     "Orc",  // targetId — phải khớp Entity.getName() của OrcMonster
 *     3,      // số lượng cần đạt
 *     50,     // thưởng vàng
 *     30      // thưởng EXP
 * );
 * </pre>
 */
public class Quest {

    private final String id;                      // định danh duy nhất, dùng để lưu save & tránh nhận trùng
    private final String title;                    // tên hiện trên UI
    private final String offerDialog;               // lời thoại khi NPC giao việc (chưa nhận)
    private final String turnInDialog;               // lời thoại khi trả thưởng
    private final QuestObjectiveType objectiveType;
    private final String targetId;                  // tên quái (KILL_MONSTER) hoặc id item (COLLECT_ITEM)
    private final int targetAmount;
    private final int rewardGold;
    private final int rewardExp;
    private Item rewardItem;                        // optional — null nếu quest không thưởng item

    public Quest(String id, String title, String offerDialog, String turnInDialog,
                 QuestObjectiveType objectiveType, String targetId, int targetAmount,
                 int rewardGold, int rewardExp) {
        this.id = id;
        this.title = title;
        this.offerDialog = offerDialog;
        this.turnInDialog = turnInDialog;
        this.objectiveType = objectiveType;
        this.targetId = targetId;
        this.targetAmount = targetAmount;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
        this.rewardItem = null;
    }

    /** Gắn thêm item thưởng (optional, gọi ngay sau constructor). Trả về this để chain được. */
    public Quest withRewardItem(Item item) {
        this.rewardItem = item;
        return this;
    }

    // --- Getter ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getOfferDialog() { return offerDialog; }
    public String getTurnInDialog() { return turnInDialog; }
    public QuestObjectiveType getObjectiveType() { return objectiveType; }
    public String getTargetId() { return targetId; }
    public int getTargetAmount() { return targetAmount; }
    public int getRewardGold() { return rewardGold; }
    public int getRewardExp() { return rewardExp; }
    public Item getRewardItem() { return rewardItem; }

    /** Mô tả mục tiêu dạng người đọc được (dùng trong dialog/HUD quest). */
    public String describeObjective(int currentAmount) {
        String verb = (objectiveType == QuestObjectiveType.KILL_MONSTER) ? "Tiêu diệt" : "Thu thập";
        return verb + " " + targetId + ": " + currentAmount + "/" + targetAmount;
    }
}
